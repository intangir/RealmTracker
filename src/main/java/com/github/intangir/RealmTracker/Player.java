package com.github.intangir.RealmTracker;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

import com.github.intangir.RealmTracker.MainConfig.Rank;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Player extends Config {

	static public Map<String, Player> players;
	@Setter
	static private RealmTracker plugin;
	static private MainConfig config;
	static private Logger log;
	static private volatile int currentTime;
	
	private String name;
	@Getter
	private Integer onlineTime;
	@Getter
	private Integer points;
	@Getter
	private Integer bonusPoints;
	@Getter
	private Integer lastSeen;
	private Integer rankId;
	private String lastServer;
	private String currentServer;
	
	private transient boolean online;
	@Getter
	private transient Rank rank;
	private transient ProxiedPlayer player;
	private transient int nextRankPoints;
	

	public Player(RealmTracker plugin, ProxiedPlayer player) {
		File test = new File(plugin.getDataFolder() + "/players/" + player.getName() + ".yml");
		if(test.exists()) {
			log.info("moving " + player.getName() + " to " + player.getUniqueId());
			test.renameTo(new File(plugin.getDataFolder() + "/players/" + player.getUniqueId() + ".yml"));
		}

		CONFIG_FILE = new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
		
		this.player = player;
		online = true;
		
		currentTime = (int)(System.currentTimeMillis() / 1000L);

		// add defaults
		name = player.getName();
		onlineTime = new Integer(0);
		points = new Integer(0);
		bonusPoints = new Integer(0);
		nextRankPoints = 0;
		rankId = null;
		lastSeen = null;
		lastServer = "";
		currentServer = "";
	}
	
	public Player(String name, File file) {
		CONFIG_FILE = file;
		this.name = name;
		player = null;
		online = false;
		lastSeen = null;
	}

	static public void initStatic(RealmTracker plugin, MainConfig config, Logger log) {
		players = new ConcurrentHashMap<String, Player>();
		Player.plugin = plugin;
		Player.config = config;
		Player.log = log;
		currentTime = (int)(System.currentTimeMillis() / 1000L);
	}

	@Override
	public void init() {
    	debug("Loading " + name);
		try {
			super.init();
		} catch (InvalidConfigurationException e) {
			log.info("Couldn't Load profile for " + name);
		}
		
    	if(online) {
			checkAbsence();
			sumPoints();
	    	checkRank();
	    	setDisplayName();
	    	save();
	    }
	}
	
	@Override
	public void save() {
    	if(!online) {
    		return;
    	}
		
		debug("Saving " + player.getName());
		try {
			super.save();
		} catch (InvalidConfigurationException e) {
			log.info("Couldn't Save profile for " + player.getName());
		}
	}

	static public void debug(String message) {
		if(config.isDebug()) {
			log.info(message);
		}
	}
	
	static public void logOn(RealmTracker plugin, ProxiedPlayer p) {
		Player player = new Player(plugin, p);
    	synchronized(player) {
    		player.init();
    		players.put(p.getName(), player);
    	}
	}

	static public void logOut(ProxiedPlayer p) {
		Player player = find(p);
		if(player != null) {
			synchronized(player) {
				currentTime = (int)(System.currentTimeMillis() / 1000L);
				player.sumPoints();
				player.save();
				players.remove(p.getName());
			}
		}
	}
	
	static public void updateOnlineTimeAll() {
		currentTime = (int)(System.currentTimeMillis() / 1000L);
		if(plugin.getProxy().getPlayers().size() > 0) {
			if(plugin.getProxy().getPlayers().size() != players.size()) {
				log.warning("Proxy Players = " + plugin.getProxy().getPlayers().size() + " but RealmTracker players = " + players.size());
			}
			for(ProxiedPlayer proxiedPlayer : plugin.getProxy().getPlayers()) {
				Player player = find(proxiedPlayer.getName());
				if(player != null) {
					synchronized(player) {
						player.updateOnlineTime();
					}
				}
			}
		}
	}
	
	static public void countVote(String name) {
		Player player = find(name);
		synchronized(player) {
			player.countVote();
		}
		
	}

	static public void updateLastServer(ProxiedPlayer p) {
		Player player = find(p);
		synchronized(player) {
			player.updateLastServer();
		}
	}

	static public void connectLastServer(String p) {
		Player player = find(p);
		player.connectLastServer();
	}
	
	static public Player find(ProxiedPlayer p) {
		return players.get(p.getName());
	}

	static public Player find(String p) {
		return players.get(p);
	}

	public void updateOnlineTime() {
		sumPoints();
		checkRank();
		save();
	}

	public void countVote() {
		bonusPoints ++;
		sumPoints();
		checkRank();
		save();
	}

	public void updateLastServer() {
		String newServer = player.getServer().getInfo().getName();
		if(currentServer == null || !newServer.equals(currentServer)) {
			lastServer = currentServer;
			currentServer = newServer;
		}
	}

	public void connectLastServer() {
		player.connect(plugin.getProxy().getServerInfo(lastServer));
	}
	
	public void checkAbsence() {
		if(lastSeen != null && currentTime - lastSeen > config.getLongAbsence()) {
			if(config.getAbsenceAnnounce() != null && config.getAbsenceAnnounce().length() > 0) {
				plugin.delayedMessage(null, String.format(config.getAbsenceAnnounce(), player.getName(), RealmTracker.getHumanReadableTime(currentTime - lastSeen)), 2);
			}
			if(config.getAbsenceMessage() != null && config.getAbsenceMessage().length() > 0) {
				plugin.delayedMessage(player, String.format(config.getAbsenceMessage(), player.getName(), RealmTracker.getHumanReadableTime(currentTime - lastSeen)), 2);
			}
		}
		lastSeen = (int)(System.currentTimeMillis() / 1000L);
	}
	
	public void sumPoints() {
		onlineTime += (currentTime - lastSeen);
		lastSeen = currentTime;
		
		points = (int) (onlineTime * config.getTimeFactor());
		points += (int) (bonusPoints * config.getBonusFactor());
		
		debug("summed " + points + " for " + name);
	}
	
	public void checkRank() {
		debug("checking for promotion for " + name + " with " + points + " and " + nextRankPoints + " needed");
		if(points >= nextRankPoints) {
			rank = config.getRank(points);
			debug("setting rank for " + name + " to " + rank.getName());
			nextRankPoints = rank.getNextRankPoints();
			if(rankId == null || rank.getId() != rankId) {
				// they mustve earned a new rank
				debug("rank promoted for " + name);
				rankId = rank.getId();
				setDisplayName();
				if(rank.getAnnounce() != null && rank.getAnnounce().length() > 0) {
					plugin.delayedMessage(null, String.format(rank.getAnnounce(), player.getName(), rank.getName()), 2);
				}
				if(rank.getMessage() != null && rank.getMessage().length() > 0) {
					plugin.delayedMessage(player, String.format(rank.getMessage(), player.getName(), rank.getName()), 2);
				}
			}
		}
	}
	
	public void setDisplayName() {
		String dname = rank.getPrefix() + name + rank.getSuffix();
		if(dname.length() > 16) {
			player.setDisplayName(rank.getPrefix() + name.substring(0, name.length() - (dname.length() - 16)) + rank.getSuffix());
		} else {
			player.setDisplayName(dname);
		}
	}
}
