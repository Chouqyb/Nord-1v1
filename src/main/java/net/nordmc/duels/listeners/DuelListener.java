package net.nordmc.duels.listeners;

import net.nordmc.duels.NordDuels;
import net.nordmc.duels.base.Duel;
import net.nordmc.duels.base.PlayerData;
import net.nordmc.duels.base.PlayerID;
import net.nordmc.duels.utils.MessagingUtility;
import net.nordmc.duels.utils.Translator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;

public final class DuelListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent e) {

		Player player = e.getPlayer();
		PlayerData data = NordDuels.getInstance().getDataManager().getData(player.getUniqueId());
		if(data == null)return;

		if(data.getStatus() == PlayerData.PlayerStatus.WAITING_FOR_DUEL_START) {
			e.setCancelled(true);
			player.teleport(e.getFrom());
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent e) {
		e.setDeathMessage(null);
		Duel currentDuel = NordDuels.getInstance().getCurrentDuel();
		if(currentDuel == null)return;
		e.getDrops().clear();

		Player victim = e.getEntity();
		victim.setHealth(20D);

		PlayerID killer = currentDuel.getOtherPlayer(PlayerID.of(victim.getName(), victim.getUniqueId()));

		if(killer == null) {
			return;
		}


		Player killerPlayer = Bukkit.getPlayer(killer.getUuid());

		NordDuels.getInstance().getDataManager().updateData(victim.getUniqueId(), (vd)-> {
			vd.incrementDeaths();

			vd.setLastKiller(killer);

			if(killerPlayer != null) {
				vd.setLastKillerHealth(killerPlayer.getHealth());
			}
		});

		if(killerPlayer != null) {
			killerPlayer.setHealth(20D);
		}

		PlayerData data = NordDuels.getInstance().getDataManager().getData(victim.getUniqueId());
		assert data != null;

		MessagingUtility.notifyAll("&e" + killer.getName() + " &7has killed &c" + victim.getName() + " &7with &4" + Translator.formatHealth(data.getLastKillerHealth()) + "♥ &7remaining");
		MessagingUtility.notifyAll("&a" + killer.getName() + " &2won this round");
		currentDuel.giveRoundWin(killer);

	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		Entity damagerEntity = e.getDamager();

		Player damager = null;
		if(damagerEntity instanceof Player) {
			damager = (Player) damagerEntity;
		}else if(damagerEntity instanceof Projectile) {
			damager = (Player) ((Projectile)damagerEntity).getShooter();
		}

		if(damager == null)return;

		Entity damagedEntity = e.getEntity();
		if(!(damagedEntity instanceof Player))
			return;
		Player damaged = (Player) damagedEntity;

		PlayerData damagerData = NordDuels.getInstance().getDataManager().getData(damager.getUniqueId());
		PlayerData damagedData = NordDuels.getInstance().getDataManager().getData(damaged.getUniqueId());
		assert damagedData != null && damagerData != null;

		if(damagerData.getStatus() != PlayerData.PlayerStatus.IN_DUEL
						&& damagedData.getStatus() != PlayerData.PlayerStatus.IN_DUEL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setFoodLevel(20);
	}

	/*@EventHandler
	public void onProjectileThrow(ProjectileLaunchEvent e) {
		Projectile projectile = e.getEntity();
		Vector current = projectile.getVelocity();
		projectile.setVelocity(current.multiply(1.4));
	}*/
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		if(player.isOp())return;

		if(e.getBlock().getType() != Material.FIRE) {
			e.setCancelled(true);
			MessagingUtility.notify(player, "&cYou cannot do that while being in a duel !");
		}

	}


	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Block block = e.getBlock();
		if(block.getType() == Material.FIRE) {
			Bukkit.getScheduler().runTaskLater(NordDuels.getInstance(),()-> block.setType(Material.AIR), 10*20L);
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockSpread(BlockFromToEvent e) {
		if(e.getBlock().getType() == Material.FIRE || e.getToBlock().getType() == Material.FIRE) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockForm(BlockFormEvent e) {
		if(e.getBlock().getType() == Material.FIRE || e.getNewState().getType() == Material.FIRE) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBurnEvent(BlockBurnEvent e) {
		e.setCancelled(true);
	}
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent igniteEvent) {
		Block ignitingBlock = igniteEvent.getIgnitingBlock();
		if(ignitingBlock == null)return;
		if(ignitingBlock.getType().name().contains("WOOD")) {
			igniteEvent.setCancelled(true);
		}
	}
}
