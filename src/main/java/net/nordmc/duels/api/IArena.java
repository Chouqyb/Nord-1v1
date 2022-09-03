package net.nordmc.duels.api;

import net.nordmc.duels.NordDuels;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public interface IArena {

	@NonNull Optional<Location> firstLocation();

	@NonNull Optional<Location> secondLocation();

	default void linkPlayers(Player first, Player second) {
		Bukkit.getScheduler().runTask(NordDuels.getInstance(), ()-> {

			Optional<Location> firstLocation = firstLocation();
			Optional<Location> secondLocation = secondLocation();

			firstLocation.ifPresent(first::teleport);
			secondLocation.ifPresent(second::teleport);
		});

	}

}
