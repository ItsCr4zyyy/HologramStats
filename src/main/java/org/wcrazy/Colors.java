package org.wcrazy;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Colors {
    public static String formatHexColors(String input) {
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

}
