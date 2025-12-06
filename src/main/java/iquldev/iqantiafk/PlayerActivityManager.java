package iquldev.iqantiafk;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerActivityManager {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final ConfigManager configManager;

    public PlayerActivityManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player, configManager));
    }

    public void removePlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public static class PlayerData {
        private final Player player;
        private final ConfigManager configManager;
        private long lastActivityTime;
        private Location lastLocation;
        private float lastYaw;
        private float lastPitch;
        private int suspicionLevel;
        private boolean isAfk;

        private final Queue<Double> historyYaw = new LinkedList<>();
        private final Queue<Double> historyPitch = new LinkedList<>();
        private final Queue<Double> historyDist = new LinkedList<>();
        private static final int HISTORY_SIZE = 20;

        public PlayerData(Player player, ConfigManager configManager) {
            this.player = player;
            this.configManager = configManager;
            this.lastActivityTime = System.currentTimeMillis();
            this.lastLocation = player.getLocation();
            this.lastYaw = player.getLocation().getYaw();
            this.lastPitch = player.getLocation().getPitch();
            this.suspicionLevel = 0;
            this.isAfk = false;
        }

        public void updateActivity() {
            this.lastActivityTime = System.currentTimeMillis();
            if (this.isAfk) {
                this.isAfk = false;
                String msg = configManager.getMessage("afk-leave").replace("%player%", player.getName());
                if (!msg.isEmpty()) {
                    org.bukkit.Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
                }
            }
        }
        
        public boolean checkSignificantActivity(org.bukkit.event.player.PlayerMoveEvent event, double minMove, double minRot, boolean ignoreJump) {
             Location from = event.getFrom();
             Location to = event.getTo();
             if (to == null) return false;

             double deltaX = Math.abs(to.getX() - from.getX());
             double deltaY = Math.abs(to.getY() - from.getY());
             double deltaZ = Math.abs(to.getZ() - from.getZ());
             
             float yawDiff = Math.abs(to.getYaw() - from.getYaw());
             if (yawDiff > 180) yawDiff = 360 - yawDiff;
             
             double deltaRot = yawDiff + Math.abs(to.getPitch() - from.getPitch());
             
             // Check if movement is significant
             double distHorizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
             
             // Check rotation
             boolean validRot = deltaRot >= minRot;
             boolean validMove = distHorizontal >= minMove;
             
             // Jump in place check
             if (ignoreJump && distHorizontal < 0.01 && deltaY > 0) {
                 validMove = false;
             }

             // Pattern Detection (Heuristic)
             if (validRot || validMove) {
                 if (isBotPattern(yawDiff, Math.abs(to.getPitch() - from.getPitch()), distHorizontal)) {
                     // It matches a bot pattern, so we act as if it is NOT significant activity.
                     // Optionally increase suspicion here.
                     this.addSuspicion(configManager.getWeight("regular-movement"));
                     return false;
                 }
             }
             
             // If neither is significant, return false
             return validRot || validMove;
        }

        private final Queue<org.bukkit.util.Vector> historyVec = new LinkedList<>();
        private static final int HISTORY_SIZE_LONG = 60; // 3 seconds at 20 TPS

        private boolean isBotPattern(double yawDelta, double pitchDelta, double distDelta) {
            addToHistory(historyYaw, yawDelta);
            addToHistory(historyPitch, pitchDelta);
            addToHistory(historyDist, distDelta);
            // Current Location Vector
            addToHistoryVec(historyVec, player.getLocation().toVector());

            if (historyYaw.size() < HISTORY_SIZE) return false;

            // 1. Variance Check (Consistency)
            if (calculateVariance(historyYaw) < 0.05 && calculateMean(historyYaw) > 1.0) return true;
            if (calculateVariance(historyDist) < 0.0001 && calculateMean(historyDist) > 0.05) return true;

            // 2. Efficiency Check (Strafe/Snake)
            // Need longer history for this
            if (historyVec.size() >= HISTORY_SIZE_LONG) {
                if (isInefficientMove()) return true;
            }

            return false;
        }

        private boolean isInefficientMove() {
            // Re-implement correctly using Vector history
            org.bukkit.util.Vector start = historyVec.peek();
            org.bukkit.util.Vector end = ((LinkedList<org.bukkit.util.Vector>)historyVec).getLast();
            
            double netDisplacement = start.distance(end);
            
            // Sum of segments
            double totalDistance = 0;
            org.bukkit.util.Vector prev = null;
            for (org.bukkit.util.Vector current : historyVec) {
                if (prev != null) {
                    totalDistance += current.distance(prev);
                }
                prev = current;
            }
            
            // If we moved a lot (e.g. > 5 blocks) but are still close to start (< 2 blocks)
            // Ratio: net / total. If < 0.3 -> inefficient (strafe).
            if (totalDistance > 5.0 && netDisplacement < 2.0) {
                 return true; // Strafe detected
            }
            
            // Rotation Snake check? 
            // Similar logic but with Angles (requires historyYawLong which we don't have yet)
            // For now, Strafe is the biggest request.
            return false;
        }

        private void addToHistoryVec(Queue<org.bukkit.util.Vector> queue, org.bukkit.util.Vector val) {
            if (queue.size() >= HISTORY_SIZE_LONG) queue.poll();
            queue.add(val);
        }

        private void addToHistory(Queue<Double> queue, double val) {
            if (queue.size() >= HISTORY_SIZE) queue.poll();
            queue.add(val);
        }

        private double calculateMean(Queue<Double> queue) {
            double sum = 0;
            for (double d : queue) sum += d;
            return sum / queue.size();
        }

        private double calculateVariance(Queue<Double> queue) {
            double mean = calculateMean(queue);
            double temp = 0;
            for (double d : queue) temp += (d - mean) * (d - mean);
            return temp / queue.size();
        }

        public long getLastActivityTime() {
            return lastActivityTime;
        }

        public Location getLastLocation() {
            return lastLocation;
        }

        public void setLastLocation(Location lastLocation) {
            this.lastLocation = lastLocation;
        }

        public float getLastYaw() {
            return lastYaw;
        }

        public void setLastYaw(float lastYaw) {
            this.lastYaw = lastYaw;
        }

        public float getLastPitch() {
            return lastPitch;
        }

        public void setLastPitch(float lastPitch) {
            this.lastPitch = lastPitch;
        }

        public int getSuspicionLevel() {
            return suspicionLevel;
        }

        public void addSuspicion(int amount) {
            this.suspicionLevel += amount;
        }

        public void reduceSuspicion(int amount) {
            this.suspicionLevel = Math.max(0, this.suspicionLevel - amount);
        }

        public boolean isAfk() {
            return isAfk;
        }

        public void setAfk(boolean afk) {
            isAfk = afk;
        }
    }
}
