package net.nordmc.duels.base.kits;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class Kit {

	private final String name;
	private final Map<Integer, ItemStack> itemStacks = new HashMap<>();
	private final ItemStack[] armor = new ItemStack[4];

	private final String permission;

	public Kit(String name) {
		this.name = name;
		permission = "nord.kits." + name.toLowerCase();
	}

	public Kit setItemAt(int slot, ItemStack itemStack) {
		itemStacks.put(slot, itemStack);
		return this;
	}

	public String getPermission() {
		return permission;
	}

	public String getName() {
		return name;
	}

	public Kit setArmorAt(int index, ItemStack armorItemStack) {

		if(!EnchantmentTarget.ARMOR.includes(armorItemStack)) {
			throw new IllegalArgumentException("ItemStack argument cannot be a part of an armor");
		}
		armor[index] = armorItemStack;
		return this;
	}

	public ItemStack[] getArmor() {
		return armor;
	}

	public ItemStack getItemAt(int slot) {
		return itemStacks.get(slot);
	}

	public Collection<ItemStack> getContents() {
		return itemStacks.values();
	}

	public void apply(Player player) {
		player.getInventory().clear();
		itemStacks.forEach((slot, item)-> player.getInventory().setItem(slot, item));
		player.getInventory().setHelmet(armor[0]);
		player.getInventory().setChestplate(armor[1]);
		player.getInventory().setLeggings(armor[2]);
		player.getInventory().setBoots(armor[3]);
		player.updateInventory();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Kit)) return false;
		Kit kit = (Kit) o;
		return Objects.equals(name, kit.name)
						&& Objects.equals(itemStacks, kit.itemStacks)
						&& Arrays.equals(armor, kit.armor);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(name, itemStacks);
		result = 31 * result + Arrays.hashCode(armor);
		return result;
	}

}
