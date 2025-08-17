package org.wcrazy;

import org.bukkit.Bukkit;
import org.wcrazy.commands.HSCommand;
import org.wcrazy.commands.HSTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.wcrazy.listeners.JoinListener;

public class HologramStats extends JavaPlugin {

    private static HologramStats instance;
    private MySQLManager mysqlManager;
    private HologramManager hologramManager;
    private PlaceholderManager placeholderManager;
    private Updater updater;

    String serverVersion = Bukkit.getBukkitVersion().split("-")[0];
    String pluginVersion = getDescription().getVersion();

    @Override
    public void onEnable() {
        instance = this;

        boolean isModernServer = serverVersion.startsWith("1.20.5") || serverVersion.startsWith("1.20.6") || serverVersion.startsWith("1.21");
        getLogger().info("\u001B[32mAllFlagsHidder v" + pluginVersion + " running on server version " + serverVersion + "\u001B[0m");
        if (!isModernServer) {
            getLogger().info("\u001B[91mYou're using unsupported server version.\u001B[0m");
            this.setEnabled(false);
            return;
        }

        saveDefaultConfig();
        reloadConfig();

        this.mysqlManager = new MySQLManager(this);
        this.hologramManager = new HologramManager(this, mysqlManager);
        this.placeholderManager = new PlaceholderManager(this);

        mysqlManager.connect();

        getCommand("hs").setExecutor(new HSCommand(this, hologramManager));
        getCommand("hs").setTabCompleter(null);
        getCommand("hs").setTabCompleter(new HSTabCompleter(this));
        getServer().getPluginManager().registerEvents(hologramManager, this);

        placeholderManager.registerPlaceholders();

        hologramManager.cleanupOldHologramsOnStart();
        updater = new Updater(this, "ItsCr4zyyy", "HologramStats");
        updater.checkForUpdate();
        getServer().getPluginManager().registerEvents(new JoinListener(updater, this), this);
    }

    @Override
    public void onDisable() {
        placeholderManager.unregisterPlaceholders();
        mysqlManager.disconnect();
    }

    public static HologramStats getInstance() {
        return instance;
    }

    public MySQLManager getMySQLManager() {
        return mysqlManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
