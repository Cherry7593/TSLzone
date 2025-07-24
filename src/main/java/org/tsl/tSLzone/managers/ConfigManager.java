package org.tsl.tSLzone.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tsl.tSLzone.TSLzone;
import org.tsl.tSLzone.models.Zone;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final TSLzone plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ConfigManager(TSLzone plugin) {
        this.plugin = plugin;
        setupDataFile();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建data.yml文件: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadConfig() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存data.yml文件: " + e.getMessage());
        }
    }

    public void saveZone(Zone zone) {
        String path = "zones." + zone.getName();

        // 保存位置1
        if (zone.getPos1() != null) {
            dataConfig.set(path + ".pos1.world", zone.getPos1().getWorld().getName());
            dataConfig.set(path + ".pos1.x", zone.getPos1().getX());
            dataConfig.set(path + ".pos1.y", zone.getPos1().getY());
            dataConfig.set(path + ".pos1.z", zone.getPos1().getZ());
        }

        // 保存位置2
        if (zone.getPos2() != null) {
            dataConfig.set(path + ".pos2.world", zone.getPos2().getWorld().getName());
            dataConfig.set(path + ".pos2.x", zone.getPos2().getX());
            dataConfig.set(path + ".pos2.y", zone.getPos2().getY());
            dataConfig.set(path + ".pos2.z", zone.getPos2().getZ());
        }

        // 保存其他属性
        dataConfig.set(path + ".damage", zone.getDamage());
        dataConfig.set(path + ".frequency", zone.getFrequency());
        dataConfig.set(path + ".active", zone.isActive());

        saveConfig();
    }

    public void removeZone(String zoneName) {
        dataConfig.set("zones." + zoneName, null);
        saveConfig();
    }

    public Map<String, Zone> loadZones() {
        Map<String, Zone> zones = new HashMap<>();

        ConfigurationSection zonesSection = dataConfig.getConfigurationSection("zones");
        if (zonesSection == null) {
            return zones;
        }

        for (String zoneName : zonesSection.getKeys(false)) {
            try {
                ConfigurationSection zoneSection = zonesSection.getConfigurationSection(zoneName);
                if (zoneSection == null) continue;

                // 加载位置1
                Location pos1 = null;
                if (zoneSection.contains("pos1")) {
                    String worldName = zoneSection.getString("pos1.world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = zoneSection.getDouble("pos1.x");
                        double y = zoneSection.getDouble("pos1.y");
                        double z = zoneSection.getDouble("pos1.z");
                        pos1 = new Location(world, x, y, z);
                    }
                }

                // 加载位置2
                Location pos2 = null;
                if (zoneSection.contains("pos2")) {
                    String worldName = zoneSection.getString("pos2.world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = zoneSection.getDouble("pos2.x");
                        double y = zoneSection.getDouble("pos2.y");
                        double z = zoneSection.getDouble("pos2.z");
                        pos2 = new Location(world, x, y, z);
                    }
                }

                // 加载其他属性
                double damage = zoneSection.getDouble("damage", 1.0);
                int frequency = zoneSection.getInt("frequency", 5);
                boolean active = zoneSection.getBoolean("active", false);

                if (pos1 != null && pos2 != null) {
                    Zone zone = new Zone(zoneName, pos1, pos2, damage, frequency);
                    zone.setActive(active);
                    zones.put(zoneName, zone);
                }

            } catch (Exception e) {
                plugin.getLogger().warning("加载区域 " + zoneName + " 时出错: " + e.getMessage());
            }
        }

        return zones;
    }

    public void reloadConfig() {
        loadConfig();
    }
}
