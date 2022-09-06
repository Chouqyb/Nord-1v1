package net.nordmc.duels;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import lombok.Setter;
import net.nordmc.duels.base.*;
import net.nordmc.duels.commands.ArenaCommand;
import net.nordmc.duels.commands.RecordCommand;
import net.nordmc.duels.listeners.DuelListener;
import net.nordmc.duels.listeners.RegistryListener;
import net.nordmc.duels.storage.SqlConnector;
import net.nordmc.duels.utils.MessagingUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class NordDuels extends JavaPlugin {

	private static @Getter NordDuels instance;

	private @Getter SqlConnector connector;
	private @Getter PlayerDataManager dataManager;

	private @Getter @Setter @Nullable Location lobbyLocation;

	private @Getter @Setter @MonotonicNonNull Arena arena;

	private @Getter @Nullable Duel currentDuel;

	private final Map<UUID, FastBoard> boards = new HashMap<>();

	private @Getter WaitingTask waitingTask;

	@Override
	public void onEnable() {
		// Plugin startup logic
		instance = this;

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		lobbyLocation = (Location) this.getConfig().get("locations.lobby");

		dataManager = new PlayerDataManager();

		arena = Arena.of();

		connector = new SqlConnector();

		this.registerCommands();
		this.registerListeners();

		waitingTask = new WaitingTask();
		waitingTask.runTaskTimer(NordDuels.getInstance(),
						1L, 20L);

		BoardTask.start();
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		instance = null;
	}

	public void startNewDuelRound(Duel main) {
		Bukkit.getScheduler().runTaskLater(this, main::nextRound, 2L);
	}

	public void startNewDuelRound(Player first, Player second) {
		this.currentDuel = Duel.createNative(first, second);
	}

	public void setLocation(String path, Location location) {
		this.getConfig().set("locations." + path, location);
		this.saveConfig();
	}

	private void registerCommands() {
		try {
			BukkitCommandManager<CommandSender> manager = new BukkitCommandManager<>
							(this, CommandExecutionCoordinator.simpleCoordinator(),
											Function.identity(), Function.identity());

			AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(manager, CommandSender.class, (p) -> SimpleCommandMeta.empty());
			annotationParser.parse(new ArenaCommand());
			annotationParser.parse(new RecordCommand());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents(new RegistryListener(),this);
		Bukkit.getPluginManager().registerEvents(new DuelListener(),this);
	}

	public void setBoard(UUID uuid, FastBoard board) {
		boards.put(uuid, board);
	}

	public void forEachBoard(BiConsumer<UUID, FastBoard> actions) {
		boards.forEach(actions);
	}

	public void removeBoard(UUID uniqueId) {
		boards.remove(uniqueId);
	}

	public void reset() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			MessagingUtility.connectToHub(player);
		}

		BoardTask.stop();
		currentDuel = null;

		Bukkit.shutdown();
	}

}
