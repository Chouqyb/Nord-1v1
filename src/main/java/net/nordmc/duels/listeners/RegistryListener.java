package net.nordmc.duels.listeners;

import fr.mrmicky.fastboard.FastBoard;
import net.nordmc.duels.NordDuels;
import net.nordmc.duels.utils.ItemBuilder;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class RegistryListener implements Listener {

	public final static ItemStack LEAVE_ITEM =
					new ItemBuilder(Material.REDSTONE)
					.setDisplay("&cLeave")
					.glow(true)
					.build();


	@EventHandler
	public void onLogin(AsyncPlayerPreLoginEvent e) {
		UUID uuid = e.getUniqueId();
		NordDuels.getInstance().getDataManager()
						.insertNewData(uuid, e.getName());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		Location location = NordDuels.getInstance().getLobbyLocation();
		if(location == null)return;

		player.teleport(location);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
		player.getInventory().setItem(8, LEAVE_ITEM);

		NordDuels.getInstance().setBoard(player.getUniqueId(),new FastBoard(player));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		Player player = e.getPlayer();
		NordDuels.getInstance().removeBoard(player.getUniqueId());
		NordDuels.getInstance().getDataManager().removeData(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClick(PlayerInteractEvent e) {

		Player player = e.getPlayer();
		Action action = e.getAction();

		ItemStack item = e.getItem();
		if(isRightClick(action)
						&& isValid(item)
						&& item.isSimilar(LEAVE_ITEM)) {
			MessagingUtility.connectToHub(player);
		}

	}


	private boolean isRightClick(Action action) {
		return action == Action.RIGHT_CLICK_BLOCK
						|| action == Action.RIGHT_CLICK_AIR;
	}
	private boolean isValid(ItemStack itemStack) {
		return itemStack != null && itemStack.getType() != Material.AIR;
	}
}
