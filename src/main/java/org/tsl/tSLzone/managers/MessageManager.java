package org.tsl.tSLzone.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tsl.tSLzone.TSLzone;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class MessageManager {
    private final TSLzone plugin;
    private File messageFile;
    private FileConfiguration messageConfig;

    public MessageManager(TSLzone plugin) {
        this.plugin = plugin;
        setupMessageFile();
        loadMessages();
    }

    private void setupMessageFile() {
        messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            messageFile.getParentFile().mkdirs();
            try (InputStream inputStream = plugin.getResource("messages.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, messageFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建messages.yml文件: " + e.getMessage());
            }
        }
    }

    public void loadMessages() {
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
    }

    public void reloadMessages() {
        loadMessages();
    }

    private String translateColorCodes(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, Object... args) {
        String message = messageConfig.getString(path, "&c消息配置错误: " + path);
        message = translateColorCodes(message);

        if (args.length > 0) {
            message = String.format(message, args);
        }

        return message;
    }

    public String getMessageWithoutPrefix(String path, Object... args) {
        return getMessage(path, args); // 现在和getMessage相同，因为没有前缀
    }

    // 常用消息的便捷方法
    public String getNoPermission() {
        return getMessage("permission.no-permission");
    }

    public String getNoAdminPermission() {
        return getMessage("permission.no-admin-permission");
    }

    public String getPlayerOnly() {
        return getMessage("permission.player-only");
    }

    public String getPos1Set(String world, double x, double y, double z) {
        return getMessage("selection.pos1-set", world, x, y, z);
    }

    public String getPos2Set(String world, double x, double y, double z) {
        return getMessage("selection.pos2-set", world, x, y, z);
    }

    public String getZoneCreateSuccess(String zoneName) {
        return getMessage("zone.create.success", zoneName);
    }

    public String getZoneCreateInfo(double damage, int frequency) {
        return getMessage("zone.create.success-info", damage, frequency);
    }

    public String getZoneCreateTip(String zoneName) {
        return getMessage("zone.create.success-tip", zoneName);
    }

    public String getZoneStartSuccess(String zoneName) {
        return getMessage("zone.start.success", zoneName);
    }

    public String getZoneStopSuccess(String zoneName) {
        return getMessage("zone.stop.success", zoneName);
    }

    public String getZoneRemoveSuccess(String zoneName) {
        return getMessage("zone.remove.success", zoneName);
    }

    public String getConfigReloadSuccess() {
        return getMessage("config.reload-success");
    }

    public String getLoadZones(int count) {
        return getMessage("config.load-zones", count);
    }

    public String getUsageCreate() {
        return getMessage("usage.create");
    }

    public String getUsageStart() {
        return getMessage("usage.start");
    }

    public String getUsageStop() {
        return getMessage("usage.stop");
    }

    public String getUsageRemove() {
        return getMessage("usage.remove");
    }
}
