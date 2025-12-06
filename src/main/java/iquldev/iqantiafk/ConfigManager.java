package iquldev.iqantiafk;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration langConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = this.plugin.getConfig();
        saveDefaultLanguages();
        loadLanguage();
    }
    
    private void saveDefaultLanguages() {
        // Save all available languages so users can see them
        saveResource("lang/en_US.yml");
        saveResource("lang/ru_RU.yml");
    }

    private void saveResource(String path) {
        if (!new java.io.File(plugin.getDataFolder(), path).exists()) {
            plugin.saveResource(path, false);
        }
    }
    
    public void loadLanguage() {
        String lang = config.getString("language", "en_US");
        java.io.File langFile = new java.io.File(plugin.getDataFolder() + "/lang", lang + ".yml");
        
        // Final fallback if they picked a non-existent lang that we don't have a default for
        if (!langFile.exists()) {
             // Try to save it if it's in the JAR (e.g. they added de_DE to jar but not loop)
             try {
                plugin.saveResource("lang/" + lang + ".yml", false);
             } catch (IllegalArgumentException e) {
                 // Resource not found in jar, maybe it's a custom file they haven't created yet?
                 plugin.getLogger().warning("Language file " + lang + ".yml not found!");
             }
        }
        
        if (langFile.exists()) {
            langConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(langFile);
        } else {
            // Fallback to default config loading or empty to prevent NPE
             langConfig = new org.bukkit.configuration.file.YamlConfiguration();
        }
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadLanguage();
    }
    
    // ... existing methods ...

    public String getMessage(String key) {
        if (langConfig == null) return "Lang not loaded";
        String msg = langConfig.getString(key);
        if (msg == null) return "&cMissing key: " + key;
        return msg;
    }

    public java.util.Set<String> getRoleNames() {
        if (config.getConfigurationSection("roles") == null) {
            return java.util.Collections.emptySet();
        }
        return config.getConfigurationSection("roles").getKeys(false);
    }

    public int getAfkTime(String role) {
        String path = "roles." + role + ".afk-time";
        if (config.contains(path)) {
            return config.getInt(path);
        }
        return config.getInt("roles.default.afk-time", 300);
    }

    public String getAction(String role) {
        String path = "roles." + role + ".action";
        if (config.contains(path)) {
            return config.getString(path);
        }
        return config.getString("roles.default.action", "KICK");
    }

    public String getKickMessage(String role) {
        String path = "roles." + role + ".kick-message";
        if (config.contains(path)) {
            return config.getString(path);
        }
        return config.getString("roles.default.kick-message", "You have been kicked for AFK.");
    }

    public String getWarnMessage(String role) {
        String path = "roles." + role + ".warn-message";
        if (config.contains(path)) {
            return config.getString(path);
        }
        return config.getString("roles.default.warn-message", "You are about to be kicked for AFK.");
    }

    public int getCheckInterval() {
        return config.getInt("check-interval", 5);
    }

    public double getMinRotation() {
        return config.getDouble("activity-thresholds.min-rotation", 10.0);
    }
    
    public double getMinMovement() {
        return config.getDouble("activity-thresholds.min-movement", 0.1);
    }

    public boolean isIgnoreJumpInPlace() {
        return config.getBoolean("activity-thresholds.ignore-jumping-in-place", true);
    }

    public boolean isSuspicionEnabled() {
        return config.getBoolean("suspicion.enabled", true);
    }

    public int getSuspicionThreshold() {
        return config.getInt("suspicion.threshold", 20);
    }

    public int getSuspicionDecay() {
        return config.getInt("suspicion.decay", 1);
    }

    public int getWeight(String type) {
        return config.getInt("suspicion.weights." + type, 1);
    }
    
    public List<String> getSuspicionActions() {
        return config.getStringList("suspicion.actions");
    }


}
