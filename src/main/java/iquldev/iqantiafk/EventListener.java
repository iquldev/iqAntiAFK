package iquldev.iqantiafk;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class EventListener implements Listener {
    private final PlayerActivityManager activityManager;
    private final ConfigManager configManager;

    public EventListener(PlayerActivityManager activityManager, ConfigManager configManager) {
        this.activityManager = activityManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        activityManager.getPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        activityManager.removePlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        activityManager.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        PlayerActivityManager.PlayerData data = activityManager.getPlayerData(event.getPlayer());
        
        double minMove = configManager.getMinMovement();
        double minRot = configManager.getMinRotation();
        boolean ignoreJump = configManager.isIgnoreJumpInPlace();
        
        if (data.checkSignificantActivity(event, minMove, minRot, ignoreJump)) {
            data.updateActivity();
        }

        if (configManager.isSuspicionEnabled()) {
             // Heuristics remain same for now
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        activityManager.getPlayerData(event.getPlayer()).updateActivity();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            activityManager.getPlayerData((org.bukkit.entity.Player) event.getWhoClicked()).updateActivity();
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        activityManager.getPlayerData(event.getPlayer()).updateActivity();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        activityManager.getPlayerData(event.getPlayer()).updateActivity();
    }
}
