package com.github.intangir.RealmTracker;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.intangir.RealmTracker.Commands.PointsCommand;
import com.github.intangir.RealmTracker.Commands.RankCommand;
import com.github.intangir.RealmTracker.Commands.SeenCommand;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import eu.ac3_servers.dev.bvotifier.bungee.model.VotifierEvent;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import lombok.Getter;

import net.md_5.bungee.api.plugin.Plugin;

public class RealmTracker extends Plugin implements Listener
{
	@Getter
	static public MainConfig config;
	@Getter
	static private Logger log;
	public UUIDCache uuidCache;
	
	@Override
    public void onEnable() {
        config = new MainConfig(this);
        log = getLogger();
        try {
			config.init();
		} catch (InvalidConfigurationException e) {
			log.severe("Couldn't Load config.yml");
			e.printStackTrace();
		}
        
        // load the uuid cache for lookups
        uuidCache = new UUIDCache(this);
        uuidCache.init();
        
        // initialize player statics
        Player.initStatic(this, config, log);

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new SeenCommand(this, config, uuidCache));
        getProxy().getPluginManager().registerCommand(this, new RankCommand(this, config));
        getProxy().getPluginManager().registerCommand(this, new PointsCommand(this, config));
        
        getProxy().getScheduler().schedule(this, new UpdatePlayers(), config.getUpdateTime(), config.getUpdateTime(), TimeUnit.SECONDS);

    }
	
	// replace last with actual server name on ConnectOther <> last
	@EventHandler
	public void onPluginMessage(final PluginMessageEvent e) {
		if(e.getTag().equals("BungeeCord")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
			String subchannel = in.readUTF();
			if(subchannel.equals("ConnectOther")) {
				String player = in.readUTF();
				String server = in.readUTF();
				if(server.equals("last")) {
					e.setCancelled(true);
					Player.connectLastServer(player);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
    public void onLoggedIn(final PostLoginEvent e) {
		uuidCache.put(e.getPlayer().getName(), e.getPlayer().getUniqueId().toString());
		Player.logOn(this, e.getPlayer());
    }

    @EventHandler
    public void onLoggedOut(final PlayerDisconnectEvent e) {
		Player.logOut(e.getPlayer());
    }

    @EventHandler
    public void onServerSwitch(final ServerSwitchEvent e) {
    	Player.updateLastServer(e.getPlayer());
    }
    
    @EventHandler
	public void onVote(VotifierEvent e) {
    	debug("vote from " + e.getVote().getUsername() + " at " + e.getVote().getAddress() + " on " + e.getVote().getTimeStamp() + " via " + e.getVote().getServiceName());
    	Player.countVote(e.getVote().getUsername());
    }
    
	class UpdatePlayers implements Runnable {
		public void run() {
			debug("Running updatePlayers");
			Player.updateOnlineTimeAll();
		}
	}
    
    static public String getHumanReadableTime(long seconds) {

    	String len = null;
    	if(seconds > 2678400) {
    		len = (int)(seconds/2678400) + " month";
    	} else if(seconds > 604800) {
    		len = (int)(seconds/604800) + " week";
    	} else if(seconds > 86400) {
    		len = (int)(seconds/86400) + " day";
    	} else if(seconds > 3600) {
    		len = (int)(seconds/3600) + " hour";
    	} else if(seconds > 60) {
    		len = (int)(seconds/60) + " minute"; 
    	} else {
    		len = seconds + " second";
    	}
    	
    	if(!len.startsWith("1 ")) {
    		len += "s";
    	}
    	return len;
    }
    
	static public void debug(String message) {
		if(config.isDebug()) {
			log.info(message);
		}
	}
	
	public void delayedMessage(final CommandSender sender, final String message, int delay) {
		getProxy().getScheduler().schedule(this, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if(sender == null) {
					getProxy().broadcast(message);
				} else {
					sender.sendMessages(message);
				}
			}
		}, delay, TimeUnit.SECONDS);

	}

}