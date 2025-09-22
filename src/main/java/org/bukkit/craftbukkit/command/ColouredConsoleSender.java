package org.bukkit.craftbukkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;

import java.util.EnumMap;
import java.util.Map;

public class ColouredConsoleSender extends ConsoleCommandSender {
    private final org.jline.reader.LineReader reader; // Duckweed - jline update
    private final org.jline.terminal.Terminal terminal; // Duckweed - jline update
    private final Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>(ChatColor.class);
    private final ChatColor[] colors = ChatColor.values();

    // Duckweed start - allow use of ANSI attributes
    private static final char ESC = 27;
    public static String attrib(final int attr) {
        return ESC + "[" + attr + "m";
    }
    // Duckweed end

    public ColouredConsoleSender(CraftServer server) {
        super(server);
        this.reader = server.getReader();
        this.terminal = reader.getTerminal();

        // Duckweed start - use extracted JLine 0.9.x function
        replacements.put(ChatColor.BLACK, attrib(0));
        replacements.put(ChatColor.DARK_BLUE, attrib(34));
        replacements.put(ChatColor.DARK_GREEN, attrib(32));
        replacements.put(ChatColor.DARK_AQUA, attrib(36));
        replacements.put(ChatColor.DARK_RED, attrib(31));
        replacements.put(ChatColor.DARK_PURPLE, attrib(35));
        replacements.put(ChatColor.GOLD, attrib(33));
        replacements.put(ChatColor.GRAY, attrib(37));
        replacements.put(ChatColor.DARK_GRAY, attrib(0));
        replacements.put(ChatColor.BLUE, attrib(34));
        replacements.put(ChatColor.GREEN, attrib(32));
        replacements.put(ChatColor.AQUA, attrib(36));
        replacements.put(ChatColor.RED, attrib(31));
        replacements.put(ChatColor.LIGHT_PURPLE, attrib(35));
        replacements.put(ChatColor.YELLOW, attrib(33));
        replacements.put(ChatColor.WHITE, attrib(37));
        // Duckweed end
    }

    @Override
    public void sendMessage(String message) {
        if (terminal != null && !(terminal instanceof org.jline.terminal.impl.DumbTerminal)) { // Duckweed - jline update
            String result = message;

            for (ChatColor color : colors) {
                if (replacements.containsKey(color)) {
                    result = result.replaceAll(color.toString(), replacements.get(color));
                } else {
                    result = result.replaceAll(color.toString(), "");
                }
            }
            System.out.println(result + attrib(0)); // Duckweed - jline update
        } else {
            super.sendMessage(message);
        }
    }
}
