package net.nordmc.duels.storage;

import net.nordmc.duels.NordDuels;
import net.nordmc.duels.base.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Row;
import org.sql2o.data.Table;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
public class SqlConnector {
	private final @NonNull Sql2o sql2o;
	private final String tableName = "PlayersData";

	public SqlConnector() {
		FileConfiguration config = NordDuels.getInstance().getConfig();
		String host = config.getString("mysql.host");
		String database = config.getString("mysql.database");
		String user = config.getString("mysql.username");
		String password = config.getString("mysql.password");

		sql2o = new Sql2o("jdbc:mysql://" + host + ":3306/" + database, user, password);

		this.createTables();
	}

	private void createTables() {

		exec((conn)-> conn.createQuery("CREATE TABLE IF NOT EXISTS "
										+ tableName + "(NAME VARCHAR(40), UUID VARCHAR(40), WINS INT, LOSES INT, KILLS INT, DEATHS INT)")
						.executeUpdate());

	}

	public void insertNewData(PlayerData data) {
		exec((conn)-> conn.createQuery("INSERT INTO " + tableName + " (NAME, UUID, WINS, LOSES, KILLS, DEATHS) " +
						"VALUES (:name, :uuid, :wins, :loses, :kills, :deaths)")
						.addParameter("name",data.getName())
						.addParameter("uuid", data.getUuid().toString())
						.addParameter("wins", data.getWins())
						.addParameter("loses", data.getLoses())
						.addParameter("kills", data.getKills())
						.addParameter("deaths", data.getDeaths())
						.executeUpdate());
	}

	public void updateData(UUID uuid) {
		PlayerData data = NordDuels.getInstance().getDataManager().getData(uuid);
		if(data == null){
			System.out.println("DATA IN MEMORY IS NULL");
			return;
		}
		exec((conn) -> conn.createQuery("UPDATE " + tableName + " SET WINS=:wins, LOSES=:loses, KILLS=:kills, DEATHS=:deaths WHERE UUID=:uuid")
						.addParameter("uuid", uuid.toString())
						.addParameter("wins", data.getWins())
						.addParameter("loses", data.getLoses())
						.addParameter("kills", data.getKills())
						.addParameter("deaths", data.getDeaths())
						.executeUpdate());
	}

	public @NonNull CompletableFuture<@Nullable PlayerData> fetch(UUID uuid) {
		return CompletableFuture.supplyAsync(()-> {
			PlayerData inMemory = NordDuels.getInstance()
							.getDataManager().getData(uuid);

			if(inMemory != null) {
				return inMemory;
			}
			try (Connection conn = sql2o.open()) {
				Table table = conn.createQuery("SELECT * FROM " + tableName + " WHERE UUID=:uuid")
								.addParameter("uuid", uuid.toString())
								.executeAndFetchTable();
				List<Row> rows = table.rows();
				Row row  = rows.isEmpty() ? null : rows.get(0);
				if(row != null) {
					return new PlayerData(
									UUID.fromString(row.getString("UUID")),
									row.getString("NAME"),
									row.getInteger("WINS"),
									row.getInteger("LOSES"),
									row.getInteger("KILLS"),
									row.getInteger("DEATHS"));
				}
				return null;
			}
		});
	}

	public @NonNull CompletableFuture<@Nullable PlayerData> fetch(String name) {
		return CompletableFuture.supplyAsync(()-> {
			PlayerData inMemory = NordDuels.getInstance()
							.getDataManager().getData(name);

			if(inMemory != null) {
				return inMemory;
			}
			try (Connection conn = sql2o.open()) {
				Table table = conn.createQuery("SELECT * FROM " + tableName + " WHERE NAME=:name")
								.addParameter("name", name)
								.executeAndFetchTable();

				PlayerData data = null;
				for (Row row : table.rows()) {
					data = new PlayerData(UUID.fromString(row.getString("UUID")),
									row.getString("NAME"),
									row.getInteger("WINS"),
									row.getInteger("LOSES"),
									row.getInteger("KILLS"),
									row.getInteger("DEATHS"));
					break;
				}

				return data;
			}
		});
	}

	public @NonNull CompletableFuture<@NonNull PlayerData> loadData(@NonNull UUID uuid,
	                                                                @NonNull String backUpName) {
		return this.fetch(uuid).thenApply((fetched)-> {
			if(fetched == null) {
				PlayerData data = new PlayerData(uuid, backUpName);
				this.insertNewData(data);
				return data;
			}

			return fetched;
		});
	}


	private void exec(Consumer<Connection> actions) {
		try (Connection conn = sql2o.open()) {
			actions.accept(conn);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
