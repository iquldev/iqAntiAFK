package iquldev.iqantiafk;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.awt.Color;

public class Commands implements CommandExecutor {
    private final PlayerActivityManager activityManager;
    private final ConfigManager configManager;

    public Commands(PlayerActivityManager activityManager, ConfigManager configManager) {
        this.activityManager = activityManager;
        this.configManager = configManager;
    }

    private String getHeader(String text) {
        return ColorUtil.gradient(text, new Color(0, 0, 139), new Color(65, 105, 225)); // DarkBlue -> RoyalBlue
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /iqantifk /iqaa
        if (command.getName().equalsIgnoreCase("iqantiafk")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("iqantiafk.reload")) {
                    sender.sendMessage(ColorUtil.process(configManager.getMessage("no-permission")));
                    return true;
                }
                configManager.reload();
                String prefix = configManager.getMessage("prefix");
                sender.sendMessage(ColorUtil.process(prefix + configManager.getMessage("reload-success")));
                return true;
            }
            sender.sendMessage(getHeader(configManager.getMessage("help-header")));
            sender.sendMessage(ColorUtil.process(configManager.getMessage("help-reload")));
            sender.sendMessage(ColorUtil.process(configManager.getMessage("help-whoafk")));
            sender.sendMessage(ColorUtil.process(configManager.getMessage("help-activity")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("whoafk")) {
            if (!sender.hasPermission("iqantiafk.whoafk")) {
                sender.sendMessage(ColorUtil.process(configManager.getMessage("no-permission")));
                return true;
            }
            sender.sendMessage(getHeader(configManager.getMessage("whoafk-header")));
            
            boolean found = false;
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                PlayerActivityManager.PlayerData data = activityManager.getPlayerData(p);
                if (data.isAfk()) {
                    long timeAfk = (System.currentTimeMillis() - data.getLastActivityTime()) / 1000;
                    String item = configManager.getMessage("whoafk-item")
                            .replace("%player%", p.getName())
                            .replace("%time%", String.valueOf(timeAfk));
                    sender.sendMessage(ColorUtil.process(item));
                    found = true;
                }
            }
            if (!found) {
                sender.sendMessage(ColorUtil.process(configManager.getMessage("whoafk-none")));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("activity")) {
            if (!sender.hasPermission("iqantiafk.activity")) {
                sender.sendMessage(ColorUtil.process(configManager.getMessage("no-permission")));
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.process(configManager.getMessage("activity-usage")));
                return true;
            }
            Player target = org.bukkit.Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ColorUtil.process(configManager.getMessage("player-not-found")));
                return true;
            }
            PlayerActivityManager.PlayerData data = activityManager.getPlayerData(target);
            long secondsSinceLast = (System.currentTimeMillis() - data.getLastActivityTime()) / 1000;

            String header = configManager.getMessage("activity-header").replace("%player%", target.getName());
            sender.sendMessage(getHeader(header));
            
            String status = data.isAfk() ? configManager.getMessage("activity-status-afk") : configManager.getMessage("activity-status-active");
            sender.sendMessage(ColorUtil.process(configManager.getMessage("activity-status").replace("%status%", status)));
            
            sender.sendMessage(ColorUtil.process(configManager.getMessage("activity-last-active").replace("%time%", String.valueOf(secondsSinceLast))));
            
            String suspLevel = data.getSuspicionLevel() > 0 ? "&c" + data.getSuspicionLevel() : configManager.getMessage("activity-none");
            sender.sendMessage(ColorUtil.process(configManager.getMessage("activity-suspicion").replace("%level%", suspLevel)));
            
            Location loc = target.getLocation();
            String locStr = configManager.getMessage("activity-location")
                    .replace("%x%", String.valueOf(loc.getBlockX()))
                    .replace("%y%", String.valueOf(loc.getBlockY()))
                    .replace("%z%", String.valueOf(loc.getBlockZ()));
            sender.sendMessage(ColorUtil.process(locStr));
            return true;
        }

        return false;
    }
}
