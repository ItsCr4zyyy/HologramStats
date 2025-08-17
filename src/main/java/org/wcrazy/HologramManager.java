package org.wcrazy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.NamespacedKey;

import java.util.*;
import java.util.stream.Collectors;

import static org.wcrazy.Colors.formatHexColors;

public class HologramManager implements Listener {

    private final HologramStats plugin;
    private final MySQLManager mysqlManager;

    private final Map<UUID, Map<String, Integer>> playerHoloIndex = new HashMap<>();

    private final Map<UUID, Map<String, List<ArmorStand>>> playerHolograms = new HashMap<>();

    private final Map<ArmorStand, String> standToKey = new HashMap<>();

    public HologramManager(HologramStats plugin, MySQLManager mysqlManager) {
        this.plugin = plugin;
        this.mysqlManager = mysqlManager;
    }

    public void spawnHologram(Player player, String hologramKey) {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("holograms." + hologramKey)) {
            player.sendMessage("§cHologram '" + hologramKey + "' is not defined.");
            return;
        }

        String locString = config.getString("holograms." + hologramKey + ".location");
        if (locString == null) {
            player.sendMessage("§cHologram '" + hologramKey + "' has no location set.");
            return;
        }

        String[] parts = locString.split(";");
        if (parts.length != 4) {
            player.sendMessage("§cHologram '" + hologramKey + "' has an invalid location.");
            return;
        }

        Location baseLoc = new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );

        removeHologram(player, hologramKey);

        List<String> rawLines = config.getStringList("holograms." + hologramKey + ".rows");

        List<String> processedLines = rawLines.stream().map(line -> {
            String l = line.replace("%player%", player.getName())
                    .replace("%player_uuid%", player.getUniqueId().toString());

            for (String qKey : config.getConfigurationSection("queries").getKeys(false)) {
                String placeholder = "%" + qKey + "%";
                if (l.contains(placeholder)) {
                    String res = mysqlManager.getQueryResult(player.getUniqueId(), qKey);
                    l = l.replace(placeholder, res);
                }
            }

            return formatHexColors(l);
        }).collect(Collectors.toList());

        List<ArmorStand> stands = new ArrayList<>();
        for (int i = 0; i < processedLines.size(); i++) {
            String line = processedLines.get(i);
            Location loc = baseLoc.clone().add(0, -0.25 * i, 0);

            ArmorStand as = player.getWorld().spawn(loc, ArmorStand.class, holo -> {
                holo.setVisible(false);
                holo.setGravity(false);
                holo.setInvulnerable(true);
                holo.setCustomName(line);
                holo.setCustomNameVisible(true);
                holo.setMarker(true);

                NamespacedKey key = new NamespacedKey(plugin, "hologram-id");
                holo.getPersistentDataContainer().set(key, PersistentDataType.STRING, hologramKey);
            });

            stands.add(as);
            standToKey.put(as, hologramKey);
        }

        playerHolograms.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(hologramKey, stands);

        stands.forEach(as -> {
            Bukkit.getOnlinePlayers().forEach(other -> other.hideEntity(plugin, as));
            player.showEntity(plugin, as);
        });
    }


    public void removeHologram(Player player, String hologramKey) {
        UUID uuid = player.getUniqueId();
        Map<String, List<ArmorStand>> holograms = playerHolograms.get(uuid);

        if (holograms == null || !holograms.containsKey(hologramKey)) return;

        List<ArmorStand> stands = holograms.get(hologramKey);
        NamespacedKey key = new NamespacedKey(plugin, "hologram-id");

        for (ArmorStand stand : stands) {
            stand.getPersistentDataContainer().remove(key);
            standToKey.remove(stand);
            stand.remove();
        }

        holograms.remove(hologramKey);
        if (holograms.isEmpty()) {
            playerHolograms.remove(uuid);
        }
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        for (Map.Entry<UUID, Map<String, List<ArmorStand>>> entry : playerHolograms.entrySet()) {
            UUID otherUUID = entry.getKey();
            if (otherUUID.equals(player.getUniqueId())) continue;

            for (List<ArmorStand> stands : entry.getValue().values()) {
                for (ArmorStand stand : stands) {
                    player.hideEntity(plugin, stand);
                }
            }
        }

        plugin.getConfig().getConfigurationSection("holograms").getKeys(false)
                .forEach(hk -> {
                    if (plugin.getConfig().getBoolean("holograms." + hk + ".show-on-join")) {
                        spawnHologram(player, hk);
                    }
                });
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uid = e.getPlayer().getUniqueId();
        if (!playerHolograms.containsKey(uid)) return;
        playerHolograms.get(uid).values().forEach(list -> list.forEach(ArmorStand::remove));
        playerHolograms.remove(uid);
        playerHoloIndex.remove(uid);
    }

    public void cleanupOldHologramsOnStart() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            NamespacedKey holoKey = new NamespacedKey(plugin, "hologram-id");

            for (var world : Bukkit.getWorlds()) {
                world.getEntitiesByClass(ArmorStand.class).stream()
                        .filter(as -> {
                            PersistentDataContainer container = as.getPersistentDataContainer();
                            return container.has(holoKey, PersistentDataType.STRING);
                        })
                        .forEach(ArmorStand::remove);
            }

            playerHolograms.clear();
        });
    }
}
