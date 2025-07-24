package org.tsl.tSLzone.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.tsl.tSLzone.TSLzone;
import org.tsl.tSLzone.models.Zone;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {
    private final TSLzone plugin;
    private final Map<String, Zone> zones;
    private final Map<String, ScheduledTask> runningTasks; // 改为ScheduledTask
    private final Map<String, Location> playerSelections; // 存储玩家的选点

    public ZoneManager(TSLzone plugin) {
        this.plugin = plugin;
        this.zones = new ConcurrentHashMap<>();
        this.runningTasks = new ConcurrentHashMap<>(); // 改为ScheduledTask
        this.playerSelections = new ConcurrentHashMap<>();

        // 从配置文件加载区域
        loadZones();
    }

    private void loadZones() {
        Map<String, Zone> loadedZones = plugin.getConfigManager().loadZones();
        zones.putAll(loadedZones);

        // 自动启动已激活的区域
        for (Zone zone : zones.values()) {
            if (zone.isActive()) {
                startZone(zone.getName());
            }
        }

        plugin.getLogger().info(plugin.getMessageManager().getLoadZones(zones.size()));
    }

    public void setPlayerPos1(String playerName, Location location) {
        String key = playerName + "_pos1";
        playerSelections.put(key, location);
    }

    public void setPlayerPos2(String playerName, Location location) {
        String key = playerName + "_pos2";
        playerSelections.put(key, location);
    }

    public Location getPlayerPos1(String playerName) {
        return playerSelections.get(playerName + "_pos1");
    }

    public Location getPlayerPos2(String playerName) {
        return playerSelections.get(playerName + "_pos2");
    }

    public boolean createZone(String zoneName, String playerName, double damage, int frequency) {
        if (zones.containsKey(zoneName)) {
            return false; // 区域已存在
        }

        Location pos1 = getPlayerPos1(playerName);
        Location pos2 = getPlayerPos2(playerName);

        if (pos1 == null || pos2 == null) {
            return false; // 选点不完整
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            return false; // 两个点不在同一世界
        }

        Zone zone = new Zone(zoneName, pos1, pos2, damage, frequency);

        // 检查区域大小限制
        if (zone.getSize() > 500 * 500 * 500) {
            return false; // 区域太大
        }

        zones.put(zoneName, zone);
        plugin.getConfigManager().saveZone(zone);

        // 清除玩家选点
        playerSelections.remove(playerName + "_pos1");
        playerSelections.remove(playerName + "_pos2");

        return true;
    }

    public boolean startZone(String zoneName) {
        Zone zone = zones.get(zoneName);
        if (zone == null || zone.isActive()) {
            return false;
        }

        zone.setActive(true);
        plugin.getConfigManager().saveZone(zone);

        // 使用Folia的RegionScheduler
        Location center = zone.getCenter();
        if (center == null || center.getWorld() == null) {
            return false;
        }

        // 创建伤害任务 - 使用Folia的RegionScheduler
        ScheduledTask task; // 改为ScheduledTask
        try {
            task = Bukkit.getRegionScheduler().runAtFixedRate(plugin, center, scheduledTask -> {
                damageEntitiesInZone(zone);
            }, 20L, zone.getFrequency() * 20L);
        } catch (Exception e) {
            plugin.getLogger().severe("无法启动区域调度器: " + e.getMessage());
            zone.setActive(false);
            return false;
        }

        runningTasks.put(zoneName, task);
        return true;
    }

    public boolean stopZone(String zoneName) {
        Zone zone = zones.get(zoneName);
        if (zone == null || !zone.isActive()) {
            return false;
        }

        zone.setActive(false);
        plugin.getConfigManager().saveZone(zone);

        // 停止任务
        ScheduledTask task = runningTasks.remove(zoneName); // 改为ScheduledTask
        if (task != null) {
            task.cancel();
        }

        return true;
    }

    public boolean removeZone(String zoneName) {
        Zone zone = zones.get(zoneName);
        if (zone == null) {
            return false;
        }

        // 先停止区域
        if (zone.isActive()) {
            stopZone(zoneName);
        }

        // 从内存和配置文件中删除
        zones.remove(zoneName);
        plugin.getConfigManager().removeZone(zoneName);

        return true;
    }

    private void damageEntitiesInZone(Zone zone) {
        Location center = zone.getCenter();
        if (center == null || center.getWorld() == null) {
            return;
        }

        // 获取区域内的所有实体
        for (Entity entity : center.getWorld().getEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (zone.isInZone(entity.getLocation())) {
                    // 直接对实体造成伤害，移除Folia特定的调度器调用
                    try {
                        livingEntity.damage(zone.getDamage());
                    } catch (Exception e) {
                        plugin.getLogger().warning("对实体造成伤害时出错: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void stopAllZones() {
        for (String zoneName : zones.keySet()) {
            stopZone(zoneName);
        }
    }

    public void saveData() {
        for (Zone zone : zones.values()) {
            plugin.getConfigManager().saveZone(zone);
        }
    }

    public Zone getZone(String zoneName) {
        return zones.get(zoneName);
    }

    public Map<String, Zone> getAllZones() {
        return new HashMap<>(zones);
    }

    public void reloadZones() {
        // 停止所有活动区域
        stopAllZones();

        // 清空当前区域
        zones.clear();

        // 重新加载
        loadZones();
    }
}
