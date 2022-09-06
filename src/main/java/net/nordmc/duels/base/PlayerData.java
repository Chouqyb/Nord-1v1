package net.nordmc.duels.base;

import lombok.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

@EqualsAndHashCode
@Getter
@Setter
public final class PlayerData {

	private final @NonNull UUID uuid;
	private final @NonNull String name;

	public PlayerData(@NonNull UUID uuid, @NonNull String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public PlayerData(@NonNull UUID uuid, @NonNull String name, int wins, int loses, int kills, int deaths) {
		this.uuid = uuid;
		this.name = name;
		this.wins = wins;
		this.loses = loses;
		this.kills = kills;
		this.deaths = deaths;
	}

	private @Getter @Nullable @Setter PlayerID lastKiller;
	private @Getter @Setter double lastKillerHealth;
	private int wins, loses, kills, deaths;

	@EqualsAndHashCode.Exclude
	private @NonNull PlayerStatus status = PlayerStatus.WAITING;

	public void incrementWins() {
		setWins(wins+1);
	}

	public void incrementLoses() {
		setLoses(loses+1);
	}

	public void incrementKills() {
		setKills(kills+1);
	}

	public void incrementDeaths() {
		setDeaths(deaths+1);
	}

	public enum PlayerStatus {

		WAITING,

		WAITING_FOR_DUEL_START,

		IN_DUEL,

		DUEL_END
	}

}
