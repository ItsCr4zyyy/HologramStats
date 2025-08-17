package org.wcrazy.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wcrazy.Updater;

import static org.wcrazy.Colors.formatHexColors;

public class JoinListener implements Listener {

    private final Updater updater;
    private final JavaPlugin plugin;

    public JoinListener(Updater updater, JavaPlugin plugin) {
        this.updater = updater;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("hologramstats.notify") && updater.isUpdateAvailable() && plugin.getConfig().getBoolean("updater", true)) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(
                    Bukkit.getPluginManager().getPlugin("HologramStats"),
                    () -> {
                        player.sendMessage(formatHexColors("&4&lHologramStats is outdated!"));
                        player.sendMessage(formatHexColors("&fCurrent version: &c" + updater.getCurrentVersion().toString()));
                        player.sendMessage(formatHexColors("&fLatest version: &a" + updater.getLatestVersion().toString()));
                        player.sendMessage(formatHexColors("&7Download here: &3" + updater.getReleaseUrl().toString()));
                        player.sendMessage(formatHexColors("&r"));
                    }, 40L
            );
        }
    }
}
