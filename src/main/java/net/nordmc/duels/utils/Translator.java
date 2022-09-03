package net.nordmc.duels.utils;

import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public final class Translator {

	static DecimalFormat format = new DecimalFormat();
	static {
		format.setMaximumFractionDigits(2);
	}

	private Translator () {}

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static List<String> colorList(List<String> list) {
		return list.stream().map(Translator::color).collect(Collectors.toList());
	}

	public static String formatHealth(double health) {
		return format.format(health);
	}

}
