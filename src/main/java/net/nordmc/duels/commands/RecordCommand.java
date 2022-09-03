package net.nordmc.duels.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RecordCommand {

	@CommandMethod(value = "record <name>", requiredSender = Player.class)
	@CommandDescription("Sees the records of another player")
	public void showRecord(Player sender, @NonNull @Argument("name")String name) {

		NordDuels.getInstance().getConnector().fetch(name).whenComplete((data, ex)-> {

			if(ex != null) {
				ex.printStackTrace();
				return;
			}
			if(data == null)
				MessagingUtility.notify(sender, "&cThat user is unknown!");
			else {
				MessagingUtility.notify(sender, "&8&l&m==================");
				MessagingUtility.notify(sender, "    &3&l" + data.getName() + "'s Records");
				MessagingUtility.notify(sender, " ");
				MessagingUtility.notify(sender, "&bWins: &7" + data.getWins());
				MessagingUtility.notify(sender, "&bLoses: &7" + data.getLoses());
				MessagingUtility.notify(sender, "&bKills: &7" + data.getKills());
				MessagingUtility.notify(sender, "&bDeaths: &7" + data.getDeaths());
				MessagingUtility.notify(sender, "&8&l&m==================");
			}

		});

	}

}
