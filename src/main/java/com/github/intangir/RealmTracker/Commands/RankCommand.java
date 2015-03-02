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
public class RankCommand extends Command {

	public String command;
	public MainConfig config;
	public RealmTracker plugin;
	
	public RankCommand(RealmTracker plugin, MainConfig config) {
		super(config.getCommand("rank"), "realmtracker.rank", config.getAliases("rank"));
		command = config.getCommand("rank");
		this.config = config;
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.YELLOW + "Ranks are earned by time played and voting, the ranks are:");
		for(Rank rank : config.getRanks()) {
			sender.sendMessage(String.format("  %s%s: %s points", rank.getPrefix(), rank.getName(), rank.getPoints().toString()));
		}
		if(sender instanceof ProxiedPlayer)
		{
			Player p = Player.find((ProxiedPlayer)sender);
			sender.sendMessage(
				String.format(ChatColor.YELLOW + "Your rank is %s%s%s with %s%s%s points.",
					p.getRank().getPrefix(),
					p.getRank().getName(),
					ChatColor.YELLOW,
					p.getRank().getPrefix(),
					p.getPoints(),
					ChatColor.YELLOW));
		}
	}
}
