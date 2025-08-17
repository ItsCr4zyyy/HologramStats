package org.wcrazy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.wcrazy.HologramStats;

import java.util.ArrayList;
import java.util.List;

public class HSTabCompleter implements TabCompleter {

    private final HologramStats plugin;

    public HSTabCompleter(HologramStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("hologramstats.list")) {
                completions.add("list");
            }
            if (sender.hasPermission("hologramstats.spawn")) {
                completions.add("spawn");
            }
            if (sender.hasPermission("hologramstats.setlocation")) {
                completions.add("setlocation");
            }
            if (sender.hasPermission("hologramstats.reload")) {
                completions.add("reload");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("spawn") && sender.hasPermission("hologramstats.spawn")) {
                addHologramNames(completions);
            } else if (args[0].equalsIgnoreCase("setlocation") && sender.hasPermission("hologramstats.setlocation")) {
                addHologramNames(completions);
            }
        }
        
        return completions;
    }

    private void addHologramNames(List<String> completions) {
        ConfigurationSection hologramsSection = plugin.getConfig().getConfigurationSection("holograms");
        if (hologramsSection != null) {
            completions.addAll(hologramsSection.getKeys(false));
        }
    }
}
