package iquldev.iqantiafk;

import org.bukkit.plugin.java.JavaPlugin;

public class iqAntiAFK extends JavaPlugin {
    
    private ConfigManager configManager;
    private PlayerActivityManager activityManager;
    private AFKChecker afkChecker;

    @Override
    public void onEnable() {
        getLogger().info("iqAntiAFK is enabling...");

        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.activityManager = new PlayerActivityManager(configManager);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new EventListener(activityManager, configManager), this);

        // Register Commands
        Commands commands = new Commands(activityManager, configManager);
        getCommand("whoafk").setExecutor(commands);
        getCommand("activity").setExecutor(commands);
        getCommand("iqantiafk").setExecutor(commands);

        // Initialize and Schedule AFKChecker
        this.afkChecker = new AFKChecker(this, configManager, activityManager);
        long interval = configManager.getCheckInterval() * 20L; // Convert seconds to ticks
        getServer().getScheduler().runTaskTimer(this, afkChecker, interval, interval);
        
        getLogger().info("iqAntiAFK enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("iqAntiAFK disabled.");
    }
    
    public PlayerActivityManager getActivityManager() {
        return activityManager;
    }
    
    public ConfigManager getConfigManager() {
       return configManager;
    }
}