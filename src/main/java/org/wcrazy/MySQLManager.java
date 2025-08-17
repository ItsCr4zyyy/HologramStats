package org.wcrazy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MySQLManager {

    private final HologramStats plugin;
    private final Map<String, HikariDataSource> dataSources = new HashMap<>();

    public MySQLManager(HologramStats plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        ConfigurationSection dbSection = plugin.getConfig().getConfigurationSection("databases");
        if (dbSection == null) {
            plugin.getLogger().warning("Section 'databases' was not found in config.yml!");
            return;
        }

        for (String key : dbSection.getKeys(false)) {
            ConfigurationSection section = dbSection.getConfigurationSection(key);
            if (section == null) continue;

            HikariConfig config = new HikariConfig();
            String host = section.getString("host");
            int port = section.getInt("port");
            String database = section.getString("database");
            String username = section.getString("username");
            String password = section.getString("password");
            int maxPoolSize = section.getInt("max-pool-size", 10);

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(maxPoolSize);
            config.setPoolName("HologramDB-" + key);

            HikariDataSource ds = new HikariDataSource(config);
            dataSources.put(key, ds);

            plugin.getLogger().info("Connected to database: " + key);
        }
    }

    public void disconnect() {
        for (HikariDataSource ds : dataSources.values()) {
            if (ds != null && !ds.isClosed()) {
                ds.close();
            }
        }
        plugin.getLogger().info("Disconnected from all databases.");
    }

    public String getQueryResult(UUID uuid, String queryKey) {
        ConfigurationSection querySection = plugin.getConfig().getConfigurationSection("queries." + queryKey);
        if (querySection == null) return "Invalid query: " + queryKey;

        String database = querySection.getString("database");
        String query = querySection.getString("selection");
        if (database == null || query == null) return "Missing information in query.";

        HikariDataSource ds = dataSources.get(database);
        if (ds == null) return "Database '" + database + "' not found.";

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String playerName = player.getName();

        query = query.replace("%player_uuid%", "'" + uuid.toString() + "'");
        query = query.replace("%player%", "'" + playerName + "'");

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    return "No data.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
                return "Error while querying.";
        }
    }

    public HikariDataSource getDataSource(String databaseName) {
        return dataSources.get(databaseName);
    }

}
