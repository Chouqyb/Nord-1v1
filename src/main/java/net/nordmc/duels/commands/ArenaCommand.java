package net.nordmc.duels.commands;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaCommand {

	@CommandMethod(value = "arena setlobby", requiredSender = Player.class)
	@CommandPermission("nord.duels.arena.setlobby")
	@CommandDescription("Sets the lobby location")
	public void setLobby(Player sender) {
		Location location = sender.getLocation();
		NordDuels.getInstance().setLobbyLocation(location);
		NordDuels.getInstance().setLocation("lobby", location);
		MessagingUtility.notify(sender, "&aYou have set the lobby location !");
	}

	@CommandMethod(value = "arena setfirstspawn", requiredSender = Player.class)
	@CommandPermission("nord.duels.arena.setfirstspawn")
	@CommandDescription("Sets the first-spawn location for first player")
	public void setFirstSpawn(Player sender) {
		NordDuels.getInstance().setLocation("first-spawn", sender.getLocation());
		MessagingUtility.notify(sender, "&aYou have set the first-spawn location !");
	}

	@CommandMethod(value = "arena setsecondspawn", requiredSender = Player.class)
	@CommandPermission("nord.duels.arena.setsecondspawn")
	@CommandDescription("Sets the first-spawn location for second player")
	public void setSecondSpawn(Player sender) {
		NordDuels.getInstance().setLocation("second-spawn", sender.getLocation());
		MessagingUtility.notify(sender, "&aYou have set the second-spawn location !");
	}

}
