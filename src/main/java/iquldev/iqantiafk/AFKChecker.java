package iquldev.iqantiafk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AFKChecker implements Runnable {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final PlayerActivityManager activityManager;

    public AFKChecker(JavaPlugin plugin, ConfigManager configManager, PlayerActivityManager activityManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.activityManager = activityManager;
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        int suspicionDecay = configManager.getSuspicionDecay();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("iqantiafk.bypass")) continue;

            PlayerActivityManager.PlayerData data = activityManager.getPlayerData(player);
            
            // Decaying suspicion
            data.reduceSuspicion(suspicionDecay);
            if (configManager.isSuspicionEnabled() && data.getSuspicionLevel() >= configManager.getSuspicionThreshold()) {
                 handleSuspiciousPlayer(player, data);
            }

            // AFK Check
            long timeSinceLastActivity = (currentTime - data.getLastActivityTime()) / 1000; // seconds
            String role = getPlayerRole(player);
            int afkTime = configManager.getAfkTime(role);

            if (afkTime > 0 && timeSinceLastActivity >= afkTime) {
                if (!data.isAfk()) {
                    setAfk(player, data, true);
                    handleAfkAction(player, role);
                }
            }
        }
    }

    private String getPlayerRole(Player player) {
        String bestRole = "default";
        int maxTime = -2; // Start lower than any valid time (including -1)

        // Iterate all configured roles
        for (String roleName : configManager.getRoleNames()) {
            if (roleName.equals("default") || player.hasPermission("iqantiafk.role." + roleName)) {
                int time = configManager.getAfkTime(roleName);
                
                // -1 is effectively infinite, so it beats everything
                if (time == -1) {
                    return roleName;
                }
                
                // Otherwise look for the longest time
                if (time > maxTime) {
                    maxTime = time;
                    bestRole = roleName;
                }
            }
        }
        return bestRole;
    }

    private void setAfk(Player player, PlayerActivityManager.PlayerData data, boolean afk) {
        data.setAfk(afk);
        String msgKey = afk ? "afk-enter" : "afk-leave";
        String msg = configManager.getMessage(msgKey).replace("%player%", player.getName());
        if (!msg.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    private void handleAfkAction(Player player, String role) {
        String action = configManager.getAction(role);
        String kickMsg = ChatColor.translateAlternateColorCodes('&', configManager.getKickMessage(role));

        switch (action) {
            case "KICK":
                Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(kickMsg));
                break;
            case "WARN":
                String warnMsg = configManager.getWarnMessage(role);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', warnMsg));
                break;
            case "NOTIFY_ADMINS":
                notifyAdmins("&c[iqAntiAFK] Player " + player.getName() + " is AFK (" + role + ").");
                break;
            case "NONE":
            default:
                break;
        }
    }

    private void handleSuspiciousPlayer(Player player, PlayerActivityManager.PlayerData data) {
         for (String action : configManager.getSuspicionActions()) {
             if (action.startsWith("notify_admins ")) {
                 notifyAdmins(ChatColor.translateAlternateColorCodes('&', action.substring("notify_admins ".length()).replace("%player%", player.getName())));
             } else if (action.equals("log_suspicion")) {
                 plugin.getLogger().warning("Suspicious activity detected for " + player.getName() + ". Level: " + data.getSuspicionLevel());
             } else if (action.startsWith("kick ")) {
                 String kickReason = ChatColor.translateAlternateColorCodes('&', action.substring("kick ".length()).replace("%player%", player.getName()));
                 Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(kickReason));
             } else if (action.startsWith("console_command ")) {
                 String cmd = action.substring("console_command ".length()).replace("%player%", player.getName());
                 Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
             }
         }
         // Reset suspicion slightly to avoid spamming every tick if we just notify? 
         // Or keep it high until manual intervention? 
         // For now, we let it decay naturally or let config handle threshold logic.
         // Effectively this will spam notification every check-interval while above threshold.
    }

    private void notifyAdmins(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("iqantiafk.notify")) {
                p.sendMessage(message);
            }
        }
        plugin.getLogger().info(message);
    }
}
