package com.github.intangir.RealmTracker.Commands;

import com.github.intangir.RealmTracker.MainConfig;
import com.github.intangir.RealmTracker.MainConfig.Rank;
import com.github.intangir.RealmTracker.Player;
import com.github.intangir.RealmTracker.RealmTracker;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

@SuppressWarnings("deprecation")
public class PointsCommand extends Command {

	public String command;
	public MainConfig config;
	public RealmTracker plugin;
	
	public PointsCommand(RealmTracker plugin, MainConfig config) {
		super(config.getCommand("points"), null, config.getAliases("points"));
		command = config.getCommand("points");
		this.config = config;
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = Player.find((ProxiedPlayer)sender);
		sender.sendMessage(
			String.format(ChatColor.YELLOW + "Your rank is %s%s%s with %s%s%s points.",
				p.getRank().getPrefix(),
				p.getRank().getName(),
				ChatColor.YELLOW,
				p.getRank().getPrefix(),
				p.getPoints(),
				ChatColor.YELLOW
			));
		sender.sendMessage(
			String.format(ChatColor.YELLOW + "%s from time online and %s from voting. Next rank at %s points.",
				(int)(p.getOnlineTime() * config.getTimeFactor()),
				(int)(p.getBonusPoints() * config.getBonusFactor()),
				p.getRank().getNextRankPoints()
			));

	}
}
