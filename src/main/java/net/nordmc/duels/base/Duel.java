package net.nordmc.duels.base;

import lombok.Data;
import lombok.Getter;
import net.nordmc.duels.NordDuels;
import net.nordmc.duels.base.kits.KitManager;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public final class Duel {

	public static final int MAX_ROUNDS_WON = 3;

	private final Map<UUID, Integer> roundsWon = new HashMap<>();

	private int currentRound = 1;

	private final UUID first, second;
	private final String firstName, secondName;

	private @MonotonicNonNull @Getter Date roundStartDate;
	Duel(Player first, Player second) {

		this.first = first.getUniqueId();
		this.second = second.getUniqueId();

		this.firstName = first.getName();
		this.secondName = second.getName();

		first.setHealth(20D);
		first.setFireTicks(0);
		first.setFoodLevel(20);

		second.setHealth(20D);
		second.setFireTicks(0);
		second.setFoodLevel(20);

		NordDuels.getInstance().getArena()
						.linkPlayers(first, second);

		KitManager.getInstance().getDuelKit().apply(first);
		KitManager.getInstance().getDuelKit().apply(second);

		this.roundsWon.put(first.getUniqueId(), 0);
		this.roundsWon.put(second.getUniqueId(), 0);

		startCountdown();
	}

	public static @NonNull Duel createNative(@NonNull Player first,
	                                         @NonNull Player second) {
		return new Duel(first, second);
	}

	public void nextRound() {
		Player firstPlayer = Bukkit.getPlayer(first);
		Player secondPlayer = Bukkit.getPlayer(second);

		firstPlayer.setHealth(20D);
		firstPlayer.setFireTicks(0);
		firstPlayer.setFoodLevel(20);

		secondPlayer.setHealth(20D);
		secondPlayer.setFireTicks(0);
		secondPlayer.setFoodLevel(20);

		NordDuels.getInstance().getArena()
						.linkPlayers(firstPlayer, secondPlayer);

		KitManager.getInstance().getDuelKit().apply(firstPlayer);
		KitManager.getInstance().getDuelKit().apply(secondPlayer);

		startCountdown();
	}


	public @NonNull String getTimeFromNow() {
		if(roundStartDate == null){
			return "00:00";
		}
		long diff = System.currentTimeMillis()-roundStartDate.getTime();
		Date date = new Date(diff);
		return new SimpleDateFormat("mm:ss")
						.format(date);
	}



	private void startCountdown() {
		new DuelCountDown().runTaskTimer(NordDuels.getInstance(),1L, 20L);
	}

	private class DuelCountDown extends BukkitRunnable {

		public static final int MAX_COUNT = 3;
		private int count = MAX_COUNT;

		DuelCountDown() {
			NordDuels.getInstance().getDataManager().updateData(first,
							(d)-> d.setStatus(PlayerData.PlayerStatus.WAITING_FOR_DUEL_START));

			NordDuels.getInstance().getDataManager().updateData(second,
							(d)-> d.setStatus(PlayerData.PlayerStatus.WAITING_FOR_DUEL_START));
		}

		@Override
		public void run() {

			Player firstPlayer = Bukkit.getPlayer(first);
			Player secondPlayer = Bukkit.getPlayer(second);

			if(count < 1) {

				MessagingUtility.notifyTitle(firstPlayer,"&6&lRound &e&l#" + currentRound, null, 20, 35, 20);
				MessagingUtility.notifyTitle(secondPlayer,"&6&lRound &e&l#" + currentRound, null, 20, 35, 20);

				Bukkit.getScheduler().runTaskLater(NordDuels.getInstance(), ()-> {
					MessagingUtility.notifyTitle(firstPlayer,"&4&lFIGHT", "&c&lKill your opponent to win", 20, 35, 20);
					MessagingUtility.notifyTitle(secondPlayer,"&4&lFIGHT", "&c&lKill your opponent to win", 20, 35, 20);

					NordDuels.getInstance().getDataManager().updateData(firstPlayer.getUniqueId(), (d)-> d.setStatus(PlayerData.PlayerStatus.IN_DUEL));
					NordDuels.getInstance().getDataManager().updateData(secondPlayer.getUniqueId(), (d)-> d.setStatus(PlayerData.PlayerStatus.IN_DUEL));

					roundStartDate = new Date();

				}, 20L);

				this.cancel();
				return;
			}

			MessagingUtility.notifyTitle(firstPlayer, this.getColor() + String.valueOf(count), null, 20, 20, 20);
			MessagingUtility.notifyTitle(secondPlayer, this.getColor() + String.valueOf(count), null, 20, 20, 20);

			count--;
		}

		private ChatColor getColor() {
			if(count == 3) return ChatColor.BLUE;
			if(count == 2) return ChatColor.GREEN;
			if(count == 1) return ChatColor.YELLOW;

			return ChatColor.RED;
		}

	}

	private void giveRoundTo(UUID uuid) {
		Integer last = roundsWon.get(uuid);
		if(last != null) {
			last++;
			roundsWon.put(uuid,last);
		}
	}

	public void giveRoundWin(PlayerID player) {
		this.giveRoundTo(player.getUuid());

		int wonRoundsCount = roundsWon.get(player.getUuid());
		PlayerID other = getOtherPlayer(player);

		assert other != null;
		Player loser = Bukkit.getPlayer(other.getUuid());

		if(wonRoundsCount >= MAX_ROUNDS_WON || loser == null || !loser.isOnline()) {
			Player winner = Bukkit.getPlayer(player.getUuid());

			this.giveDuelWin(winner, loser);
			return;
		}
		currentRound++;
		NordDuels.getInstance().getDataManager()
						.updateData(player.getUuid(), PlayerData::incrementKills);


		NordDuels.getInstance().startNewDuelRound(this);
	}

	public void giveDuelWin(Player winner, Player loser) {
		NordDuels.getInstance().getDataManager().updateData(winner.getUniqueId(),(wData)->  {
			wData.incrementWins();
			wData.setStatus(PlayerData.PlayerStatus.DUEL_END);
		});

		Objects.requireNonNull(winner, "Winner cannot be null");

		if(loser != null && loser.isOnline()) {
			NordDuels.getInstance().getDataManager().updateData(loser.getUniqueId(),(lData)->  {
				lData.incrementLoses();
				lData.setStatus(PlayerData.PlayerStatus.DUEL_END);
			});

			loser.setHealth(20D);
			loser.setFoodLevel(20);
			loser.getInventory().clear();
			loser.setAllowFlight(true);
			loser.setFlying(true);
			loser.playSound(loser.getLocation(), Sound.VILLAGER_HIT,1.0f, 1.0f);
			MessagingUtility.notifyTitle(loser, "&4&lGAME OVER", "&cYou lost :(", 30, 40, 30);
		}

		winner.setHealth(20D);
		winner.setFoodLevel(20);
		winner.getInventory().clear();
		winner.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
		winner.setAllowFlight(true);
		winner.setFlying(true);
		winner.playSound(winner.getLocation(), Sound.LEVEL_UP,1.0f, 1.0f);
		winner.getInventory().clear();

		PlayerID loserID = getOtherPlayer(PlayerID.of(winner.getName(), winner.getUniqueId()));
		int winnerRoundsWon = roundsWon.get(winner.getUniqueId());

		if(loserID != null) {
			int loserRoundsWon = roundsWon.get(loserID.getUuid());
			MessagingUtility.notifyTitle(winner,"&2&lVICTORY !","&aYou won &9" + winnerRoundsWon + "&8-&c" + loserRoundsWon,30,40, 30);
		}

		//UPDATING STATS

		Bukkit.getScheduler().runTaskLater(NordDuels.getInstance(),
						()-> NordDuels.getInstance().reset(), 100L); //5 seconds delayed task
	}

	public @Nullable PlayerID getOtherPlayer(PlayerID id) {
		if(first.equals(id.getUuid())) {
			return PlayerID.of(secondName, second);
		}

		if(second.equals(id.getUuid())) {
			return PlayerID.of(firstName, first);
		}

		return null;
	}

}
