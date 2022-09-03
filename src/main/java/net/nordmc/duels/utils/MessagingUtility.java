package net.nordmc.duels.utils;

import net.minecraft.server.v1_8_R3.Packet;
import net.nordmc.duels.NordDuels;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;

public class MessagingUtility {

	public static String PREFIX = "&3&lNord&7&lMC ";

	public static void notify(CommandSender sender, String msg) {
		sender.sendMessage(Translator.color(PREFIX + msg));
	}

	public static void notifyAll(String msg) {
		for(Player player : Bukkit.getOnlinePlayers())
			notify(player,msg);
	}

	public static void notifyTitle(@NonNull Player player,
	                               @NonNull String title,
	                               @Nullable String subTitle,
	                               int fadeIn,
	                               int stay,
	                               int fadeOut) {

		title = Translator.color(title);
		if(subTitle != null) {
			subTitle = Translator.color(subTitle);
		}

		try {

			Class<?> PacketPlayOutTitleClass = Reflection.getMinecraftClass("PacketPlayOutTitle");
			Class<?> IChatBaseComponentClass = Reflection.getMinecraftClass("IChatBaseComponent");

			Constructor<?> PacketPlayOutTitleConstructor = PacketPlayOutTitleClass.getConstructor(PacketPlayOutTitleClass.getDeclaredClasses()[0], IChatBaseComponentClass, int.class, int.class, int.class);

			Object titleComponent = IChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");

			Object titlePacket = PacketPlayOutTitleConstructor.newInstance(PacketPlayOutTitleClass.getDeclaredClasses()[0].getField("TITLE").get(null), titleComponent, fadeIn, stay, fadeOut);

			sendPacket(player, (Packet<?>) titlePacket);


			if(subTitle != null) {
				Object subTitleComponent = IChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subTitle + "\"}");
				Object subTitlePacket = PacketPlayOutTitleConstructor.newInstance(PacketPlayOutTitleClass.getDeclaredClasses()[0].getField("SUBTITLE").get(null), subTitleComponent, fadeIn, stay, fadeOut);
				sendPacket(player, (Packet<?>) subTitlePacket);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendPacket(Player player, Packet<?> packet) {
		if(player == null)return;
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	public static void connectToServer(Player player, String server) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("Connect");
				out.writeUTF(server);
			} catch (Exception e) {
				e.printStackTrace();
			}
			player.sendPluginMessage(NordDuels.getInstance(), "BungeeCord", b.toByteArray());
		} catch (org.bukkit.plugin.messaging.ChannelNotRegisteredException e) {
			Bukkit.getLogger().warning(" ERROR - Usage of bungeecord connect effects is not possible. Your server is not having bungeecord support (Bungeecord channel is not registered in your minecraft server)!");
		}
	}

	public static void connectToHub(Player player ){
		connectToServer(player, NordDuels.getInstance().getConfig().getString("fallback-server"));
	}

}
