package net.nordmc.duels.base;

import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
				new TeleportationStartTask(onlinePlayers).runTaskTimer(NordDuels.getInstance(),1L,20L);
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

		private final List<Player> online = new ArrayList<>();

		private TeleportationStartTask(Collection<? extends Player> online) {

			int c = 0;
			for(Player player : online) {
				if(c > 2)break;
				this.online.add(player);
				c++;
			}

		}


		@Override
		public void run() {

			Player first = online.get(0);
			Player second = online.get(1);

			if(lastCounts < 1) {
				NordDuels.getInstance().startNewDuelRound(first, second);
				this.cancel();
				return;
			}else {
				first.playSound(first.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
				second.playSound(second.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);

				MessagingUtility.notify(first, "&aTeleporting you in " + lastCounts);
				MessagingUtility.notify(second, "&aTeleporting you in " + lastCounts);
			}
			lastCounts--;
		}

	}

}
