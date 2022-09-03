package net.nordmc.duels.base;

import net.nordmc.duels.NordDuels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlayerDataManager {

	private final ConcurrentHashMap<UUID, PlayerData> data = new ConcurrentHashMap<>();

	public @Nullable PlayerData getData(UUID uuid) {
		return data.get(uuid);
	}

	public @Nullable PlayerData getData(String name) {
		for(PlayerData d : data.values()) {
			if(d.getName().equalsIgnoreCase(name)) {
				return d;
			}
		}
		return null;
	}

	public void insertNewData(UUID uuid, String name) {
		NordDuels.getInstance().getConnector().loadData(uuid, name)
						.whenComplete((data, ex)-> {
							if(ex != null) {
								ex.printStackTrace();
								return;
							}
							this.data.put(data.getUuid(), data);
						});
	}
	public void updateData(UUID uuid, Consumer<PlayerData> consumer) {
		data.computeIfPresent(uuid, (k, v)-> {
			consumer.accept(v);

			return v;
		});

		CompletableFuture.runAsync(()->
						NordDuels.getInstance().getConnector().updateData(uuid));
	}

	public void removeData(Player player) {
		data.remove(player.getUniqueId());
		Duel duel = NordDuels.getInstance().getCurrentDuel();
		if(duel != null) {
			PlayerID other = duel.getOtherPlayer(PlayerID.of(player.getName(), player.getUniqueId()));
			if(other != null) {
				Player otherPlayer = Bukkit.getPlayer(other.getUuid());
				duel.giveDuelWin(otherPlayer, player);
			}
		}

	}

}
