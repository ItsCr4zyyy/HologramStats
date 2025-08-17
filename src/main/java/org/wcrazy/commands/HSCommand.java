package org.wcrazy.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.wcrazy.HologramManager;
import org.wcrazy.HologramStats;

import java.util.Set;

import static org.wcrazy.Colors.formatHexColors;

public class HSCommand implements CommandExecutor {

    private final HologramStats plugin;
    private final HologramManager hologramManager;
    
    public HSCommand(HologramStats plugin, HologramManager hologramManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
        plugin.getCommand("hs").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!sender.hasPermission("hologramstats.help")) {
                String noPermMsg = plugin.getConfig().getString("no-permission", "&cYou do not have permission to use this command.");
                sender.sendMessage(formatHexColors(noPermMsg));
                return true;
            }
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hologramstats.reload")) {
                String noPermMsg = plugin.getConfig().getString("no-permission", "&cYou do not have permission to use this command.");
                sender.sendMessage(formatHexColors(noPermMsg));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(formatHexColors("&aHologramStats config was reloaded successfully."));
            plugin.getLogger().info("Config was reloaded by " + sender.getName());
            return true;
        } else if (args[0].equalsIgnoreCase("spawn")) {
            if (!sender.hasPermission("hologramstats.spawn")) {
                String noPermMsg = plugin.getConfig().getString("no-permission", "&cYou do not have permission to use this command.");
                sender.sendMessage(formatHexColors(noPermMsg));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender);
                return true;
            }
            String hologramKey = args[1];
            Player player = (Player) sender;

            if (!plugin.getConfig().isConfigurationSection("holograms." + hologramKey)) {
                sender.sendMessage("§cHologram '" + hologramKey + "' does not exist in config.yml.");
                return true;
            }

            hologramManager.spawnHologram(player, hologramKey);
            sender.sendMessage("§aHologram §e" + hologramKey + " §awas spawned.");
            return true;
        } else if (args[0].equalsIgnoreCase("setlocation")) {
            if (!sender.hasPermission("hologramstats.setlocation")) {
                String noPermMsg = plugin.getConfig().getString("no-permission", "&cYou do not have permission to use this command.");
                sender.sendMessage(formatHexColors(noPermMsg));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }
            if (args.length < 2) {
                sendHelp(sender);
                return true;
            }

            String key = args[1];
            Player p = (Player) sender;
            Location loc = p.getLocation();

            FileConfiguration config = plugin.getConfig();
            config.set("holograms." + key + ".location",
                    loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ());
            plugin.saveConfig();

            sender.sendMessage("§aLocation for hologram '" + key + "' was saved.");
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("hologramstats.list")) {
                String noPermMsg = plugin.getConfig().getString("no-permission", "&cYou do not have permission to use this command.");
                sender.sendMessage(formatHexColors(noPermMsg));
                return true;
            }
            Set<String> keys = plugin.getConfig().getConfigurationSection("holograms").getKeys(false);
            sender.sendMessage(formatHexColors("&8+------------------------------------+"));
            sender.sendMessage(formatHexColors("&r &6&l★&r &fHologramStats holograms:"));
            for (String k : keys) {
                net.md_5.bungee.api.chat.TextComponent baseText = new net.md_5.bungee.api.chat.TextComponent(formatHexColors("&r &8➥ &e" + k + " "));
                
                if (sender.hasPermission("hologramstats.spawn")) {
                    net.md_5.bungee.api.chat.TextComponent spawnText = new net.md_5.bungee.api.chat.TextComponent(formatHexColors("&a[SPAWN]"));
                    spawnText.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/hs spawn " + k));
                    spawnText.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
                        new net.md_5.bungee.api.chat.ComponentBuilder("Click to spawn hologram: " + k).create()));
                    baseText.addExtra(spawnText);
                    baseText.addExtra(" ");
                }
                
                if (sender.hasPermission("hologramstats.setlocation")) {
                    net.md_5.bungee.api.chat.TextComponent locationText = new net.md_5.bungee.api.chat.TextComponent(formatHexColors("&6[SET LOCATION]"));
                    locationText.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, "/hs setlocation " + k));
                    locationText.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, 
                        new net.md_5.bungee.api.chat.ComponentBuilder("Click to set location for hologram: " + k).create()));
                    baseText.addExtra(locationText);
                }
                
                sender.spigot().sendMessage(baseText);
            }
            sender.sendMessage(formatHexColors("&8+------------------------------------+"));
            return true;
        } else {
            sendHelp(sender);
            return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(formatHexColors("&8+------------------------------------+"));
        sender.sendMessage(formatHexColors("&r &6&l★&r &fHologramStats commands:"));
        sender.sendMessage(formatHexColors("&r &8➥ &e/hs spawn <name>"));
        sender.sendMessage(formatHexColors("&r &8➥ &e/hs setlocation <name>"));
        sender.sendMessage(formatHexColors("&r &8➥ &e/hs list"));
        sender.sendMessage(formatHexColors("&r &8➥ &e/hs reload"));
        sender.sendMessage(formatHexColors("&8+------------------------------------+"));
    }
}
