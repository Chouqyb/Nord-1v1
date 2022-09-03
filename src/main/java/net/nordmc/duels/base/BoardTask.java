package net.nordmc.duels.base;

import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.Translator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BoardTask extends BukkitRunnable {

	private BoardTask() {}

	private static @MonotonicNonNull BoardTask boardTask;
	public static void start() {
		boardTask = new BoardTask();
		boardTask.runTaskTimerAsynchronously(NordDuels.getInstance(), 1L, 20L);
	}

	public static void stop() {
		boardTask.cancel();
	}

	@Override
	public void run() {

		NordDuels.getInstance().forEachBoard((uuid, board)-> {

			PlayerData data = NordDuels.getInstance().getDataManager().getData(uuid);
			if(data == null) {
				System.out.println("DATA NULL, STOPPING BOARD FOR " + uuid.toString());
				return;
			}

			board.updateTitle(Translator.color("&3&lNord&7&lDuels"));
			List<String> content = new ArrayList<>();
			content.add(Translator.color("&7&l&m+-----------------+"));
			if(data.getStatus() == PlayerData.PlayerStatus.WAITING) {

				content.addAll(Arrays.asList(Translator.color("&3&l┃ &b&lWins: " + data.getWins()),
								Translator.color("&3&l┃ &b&lLoses: " + data.getLoses()),
								Translator.color("&3&l┃ &b&lKills: " + data.getKills()),
								Translator.color("&3&l┃ &b&lDeaths: " + data.getDeaths())));

			}else {
				Duel currentDuel = NordDuels.getInstance().getCurrentDuel();
				assert currentDuel != null;
				Player thisPlayer = board.getPlayer();
				PlayerID other = currentDuel.getOtherPlayer(PlayerID.of(thisPlayer.getName(), thisPlayer.getUniqueId()));
				assert other != null;
				content.addAll(Arrays.asList(Translator.color("&b┃ &3&lRound #" + currentDuel.getCurrentRound()),
								Translator.color("&b┃ &3&lTime: &7" + currentDuel.getTimeFromNow()),
								Translator.color("&b┃ &3&lFoe: &7" + other.getName())));

			}
			content.add(Translator.color("&7&l&m+-----------------+"));
			board.updateLines(content);
		});

	}

}
