package net.nordmc.duels.base.kits;

import lombok.Getter;
import net.nordmc.duels.utils.ItemBuilder;
import net.nordmc.duels.utils.ItemEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class KitManager {

	//Kits
	//data of kits -> HashMap<>

	private static @Nullable KitManager instance;

	public static @NonNull KitManager getInstance() {
		if(instance == null) {
			instance = new KitManager();
		}
		return instance;
	}

	private final @NonNull @Getter Kit duelKit;

	public KitManager() {

		duelKit = createKit("test")
						.setArmorAt(0,new ItemBuilder(Material.IRON_HELMET).setUnbreakable(true).build())
						.setArmorAt(1, new ItemBuilder(Material.IRON_CHESTPLATE).setUnbreakable(true).build())
						.setArmorAt(2, new ItemBuilder(Material.IRON_LEGGINGS).setUnbreakable(true).build())
						.setArmorAt(3, new ItemBuilder(Material.IRON_BOOTS).setUnbreakable(true).build())
						.setItemAt(0, new ItemBuilder(Material.STONE_SWORD).setUnbreakable(true).addEnchants(ItemEnchant.of(Enchantment.DAMAGE_ALL, 1)).build())
						.setItemAt(1, new ItemBuilder(Material.FISHING_ROD).setUnbreakable(true).build())
						.setItemAt(2, new ItemBuilder(Material.BOW).setUnbreakable(true).build())
						.setItemAt(9,new ItemBuilder(Material.ARROW, 5).build())
						.setItemAt(8, new ItemBuilder(Material.FLINT_AND_STEEL, 1, (short)64).build());


	}


	public Kit createKit(String name)  {
		return new Kit(name);
	}


}
