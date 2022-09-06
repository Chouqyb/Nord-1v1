package net.nordmc.duels.base;

import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Collection;

public final class WaitingTask extends BukkitRunnable {

	public static int MAX_SECONDS = 300;

	private int count = MAX_SECONDS;
	@Override
	public void run() {

		if(count <= 0) {
			this.cancel();
			Bukkit.getServer().shutdown();
		}else {

			Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
			if(onlinePlayers.isEmpty()) return;

			if(onlinePlayers.size() == 2) {
				new TeleportationStartTask().runTaskTimer(NordDuels.getInstance(),1L,20L);
				this.cancel();
				return;
			}

			if(count%60==0) {
				MessagingUtility.notifyAll("&aWaiting for " + (2-onlinePlayers.size()) + " more players to enter");
			}

			count--;
		}

	}

	private static class TeleportationStartTask extends BukkitRunnable {

		private int lastCounts = 3;

		private TeleportationStartTask() {
		}


		@Override
		public void run() {

			if(lastCounts < 1) {

				if(Bukkit.getOnlinePlayers().size() < 2) {

					this.cancel();
					int diff = 2-Bukkit.getOnlinePlayers().size();
					MessagingUtility.notifyAll("&cNo Enough players, waiting for " + diff + "more players to join...");
					return;
				}
				Player[] players = new Player[2];
				int i = 0;
				for(Player player : Bukkit.getOnlinePlayers()) {
					players[i] = player;
					i++;
				}
				NordDuels.getInstance().startNewDuelRound(players[0], players[1]);
				this.cancel();
				return;
			}else {

				Bukkit.getOnlinePlayers().forEach(p -> {
					p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
					MessagingUtility.notify(p, "&aStarting in " + lastCounts);
				});
			}
			lastCounts--;
		}

	}

}
