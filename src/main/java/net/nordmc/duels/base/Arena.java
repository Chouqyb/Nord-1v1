package net.nordmc.duels.base;

import lombok.Setter;
import net.nordmc.duels.NordDuels;
import net.nordmc.duels.api.IArena;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Optional;

public final class Arena implements IArena {

	private @Nullable @Setter Location firstLoc, secondLoc;
	private Arena() {
		firstLoc = (Location) NordDuels.getInstance().getConfig().get("locations.first-spawn");
		secondLoc = (Location) NordDuels.getInstance().getConfig().get("locations.second-spawn");
	}

	public static Arena of() {
		return new Arena();
	}

	@Override
	public @NonNull Optional<Location> firstLocation() {
		return Optional.ofNullable(firstLoc);
	}

	@Override
	public @NonNull Optional<Location> secondLocation() {
		return Optional.ofNullable(secondLoc);
	}

}
