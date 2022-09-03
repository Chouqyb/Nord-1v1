package net.nordmc.duels.base;

import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

@Data(staticConstructor = "of")
public class PlayerID {

	private final @NonNull String name;
	private final @NonNull UUID uuid;

}
