package com.github.intangir.RealmTracker.Commands;

import java.io.File;

import com.github.intangir.RealmTracker.MainConfig;
import com.github.intangir.RealmTracker.Player;
import com.github.intangir.RealmTracker.RealmTracker;
import com.github.intangir.RealmTracker.UUIDCache;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

@SuppressWarnings("deprecation")
public class SeenCommand extends Command {

	public String command;
	public MainConfig config;
	public UUIDCache uuids;
	public RealmTracker plugin;
	
	public SeenCommand(RealmTracker plugin, MainConfig config, UUIDCache uuids) {
		super(config.getCommand("seen"), "realmtracker.seen", config.getAliases("seen"));
		command = config.getCommand("seen");
		this.config = config;
		this.uuids = uuids;
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 1) {
			String name = args[0];
			if(plugin.getProxy().getPlayer(name) != null) {
				sender.sendMessage(String.format(config.getOnlineSeenMessage(), name));
				return;
			}

			String id = uuids.get(name);
			if(id == null) {
				id = name;
			}
			RealmTracker.debug("Looking up " + name + " found id " + id);
			File profile = new File(plugin.getDataFolder() + "/players/" + id + ".yml");
			if(!profile.exists()) {
				RealmTracker.debug("  found no player file");
				sender.sendMessage(String.format(config.getNeverSeenMessage(), name));
			} else {
				RealmTracker.debug("  found player file");
				Player player = new Player(name, profile);
				player.init();
				if(player != null && player.getLastSeen() != null) {
					RealmTracker.debug("  player file loaded");
					sender.sendMessage(String.format(config.getLastSeenMessage(), name, RealmTracker.getHumanReadableTime((System.currentTimeMillis() / 1000L) - player.getLastSeen())));
				} else {
					RealmTracker.debug("  player file failed to load");
					sender.sendMessage(String.format(config.getNeverSeenMessage(), name));
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Usage: /" + command + " [name]");
		}
	}
}
