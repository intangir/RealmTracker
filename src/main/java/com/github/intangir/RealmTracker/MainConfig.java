package com.github.intangir.RealmTracker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class MainConfig extends Config {

	public MainConfig() {};
	
	@Comment("Print debugging information")
	private boolean debug;
	
	@Getter
	@Comment("Ranks and their threshholds")
	private List<Rank> ranks;

	@Comment("time gone in seconds considered a long absense")
	private Integer longAbsence;

	@Getter
	@Comment("time in seconds between promotion checks")
	private Integer updateTime;

	@Comment("announcement message when someone returns after long absence")
	private String absenceAnnounce;

	@Comment("private message when someone returns after long absence")
	private String absenceMessage;
	
	@Comment("message for describing when someone was last seen")
	private String lastSeenMessage;

	@Comment("message for describing if someone was never seen")
	private String neverSeenMessage;

	@Comment("message for describing someone is on now for seen")
	private String onlineSeenMessage;

	@Comment("time (seconds) to points conversion factor")
	private Double timeFactor;

	@Comment("bonus to points conversion factor")
	private Double bonusFactor;
	
	@Comment("List of commands")
	private Map<String, String> commands;

	public MainConfig(Plugin plugin) {
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");

		debug = false;
		
		ranks = new ArrayList<Rank>();
		ranks.add(new Rank(0, "Newb", 0, ChatColor.GRAY.toString(), "", ChatColor.LIGHT_PURPLE + "%s has joined for the first time!", ChatColor.YELLOW + "Welcome %s!"));
		ranks.add(new Rank(1, "Master", 1000, ChatColor.RED.toString(), "", ChatColor.LIGHT_PURPLE + "%s has achieved %s!", ChatColor.YELLOW + "Congrats! %s!"));
		
		longAbsence = new Integer(604800);
		updateTime = new Integer(60);
		
		absenceAnnounce = "%s has returned after %s!";
		absenceMessage = "Welcome back %s!";
		
		lastSeenMessage = ChatColor.YELLOW + "%s was last seen %s ago.";
		neverSeenMessage = ChatColor.YELLOW + "%s has never been seen.";
		onlineSeenMessage = ChatColor.YELLOW + "%s is online now!";
		
		timeFactor = new Double(0.000278);
		bonusFactor = new Double(1);
		
		commands = new HashMap<String, String>();
		commands.put("seen", "seen lastseen laston lookup");
		commands.put("rank", "rank ranks");
		commands.put("points", "points");

	}
	
	@Override
	public void init() throws InvalidConfigurationException {
		super.init();
		
		Rank prev = null;
		for(Rank rank : ranks) {
			if(prev != null) {
				prev.setNextRankPoints(rank.getPoints());
				rank.setLastRank(prev);
			}
			prev = rank;
		}
		prev.setNextRankPoints(1000000);
	}
	
	@Getter
	public class Rank extends Config {
		public Rank() {}
		public Rank(int id, String name, int points, String prefix, String suffix, String announce, String message) {
			this.id = id;
			this.name = name;
			this.points = points;
			this.prefix = prefix;
			this.suffix = suffix;
			this.announce = announce;
			this.message = message;
		}
		
		private Integer id;
		private String name;
		private Integer points;
		private String prefix;
		private String suffix;
		private String announce;
		private String message;
		
		@Setter
		private transient int nextRankPoints;
		@Setter
		private transient Rank lastRank;
	};
	
	public Rank getRank(int points) {
		//RealmTracker.debug("get rank for points " + points);
		for(Rank rank : ranks) {
			//RealmTracker.debug("checking rank " + rank.getName());
			if(points < rank.getPoints()) {
				return rank.getLastRank();
			}
		}
		return ranks.get(ranks.size() - 1);
	}
	
	public String getCommand(String key) {
		if(!commands.containsKey(key)) {
			commands.put(key, key);
			try {
				save();
			} catch (InvalidConfigurationException ignore) {
			}
		}
		return commands.get(key).split(" ")[0];
	}
	
	public String[] getAliases(String key) {
		return commands.get(key).split(" ");
	}

}
