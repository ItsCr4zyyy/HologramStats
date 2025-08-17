package org.wcrazy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariDataSource;

public class PlaceholderManager extends PlaceholderExpansion {
    
    private final HologramStats plugin;
    private final MySQLManager mysqlManager;
    private final Map<String, QueryInfo> queries = new HashMap<>();
    
    public PlaceholderManager(HologramStats plugin) {
        this.plugin = plugin;
        this.mysqlManager = plugin.getMySQLManager();
        loadQueriesFromConfig();
    }
    
    @Override
    public String getIdentifier() {
        return "hologramstats";
    }
    
    @Override
    public String getAuthor() {
        return "wCrazy_";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        if (queries.containsKey(params)) {
            return executeQuery(player, params);
        }
        
        return "";
    }
    
    private void loadQueriesFromConfig() {
        ConfigurationSection queriesSection = plugin.getConfig().getConfigurationSection("queries");
        if (queriesSection == null) {
            plugin.getLogger().warning("Section 'queries' was not found in config.yml!");
            return;
        }
        
        for (String queryName : queriesSection.getKeys(false)) {
            ConfigurationSection querySection = queriesSection.getConfigurationSection(queryName);
            if (querySection != null) {
                String database = querySection.getString("database", "");
                String selection = querySection.getString("selection", "");
                
                if (!database.isEmpty() && !selection.isEmpty()) {
                    queries.put(queryName, new QueryInfo(database, selection));
                    plugin.getLogger().info("Loaded query: " + queryName + " for database: " + database);
                }
            }
        }
        plugin.getLogger().info("Total loaded " + queries.size() + " queries for placeholders.");
    }
    
    private String executeQuery(Player player, String queryName) {
        QueryInfo queryInfo = queries.get(queryName);
        if (queryInfo == null) {
            return "0";
        }
        
        try {
            String query = queryInfo.selection
                .replace("%player%", "'" + player.getName() + "'")
                .replace("%player_uuid%", "'" + player.getUniqueId().toString() + "'")
                .replace("%player_name%", "'" + player.getName() + "'");
            
            return executeDatabaseQuery(queryInfo.database, query);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error while executing query '" + queryName + "': " + e.getMessage());
            return "0";
        }
    }
    
    private String executeDatabaseQuery(String databaseName, String query) {
        try {
            HikariDataSource dataSource = mysqlManager.getDataSource(databaseName);
            if (dataSource == null) {
                plugin.getLogger().warning("Could not obtain connection to database: " + databaseName);
                return "0";
            }
            
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Object value = resultSet.getObject(1);
                            return value != null ? value.toString() : "0";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQL error while executing query: " + e.getMessage());
        }
        
        return "0";
    }
    
    public CompletableFuture<String> getPlaceholderAsync(Player player, String placeholder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return onPlaceholderRequest(player, placeholder);
            } catch (Exception e) {
                plugin.getLogger().warning("Error during asynchronous placeholder processing: " + e.getMessage());
                return "";
            }
        });
    }
    
    public void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            register();
            plugin.getLogger().info("PlaceholderAPI placeholders successfully registered! (" + queries.size() + " queries)");
        } else {
            plugin.getLogger().warning("PlaceholderAPI was not found! Placeholders will not work.");
        }
    }
    
    public void unregisterPlaceholders() {
        try {
            unregister();
            plugin.getLogger().info("PlaceholderAPI placeholders successfully unregistered!");
        } catch (Exception e) {
            plugin.getLogger().warning("Error while unregistering placeholders: " + e.getMessage());
        }
    }
    
    private static class QueryInfo {
        final String database;
        final String selection;
        
        QueryInfo(String database, String selection) {
            this.database = database;
            this.selection = selection;
        }
    }
}
