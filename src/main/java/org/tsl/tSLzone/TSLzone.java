package org.tsl.tSLzone;

import org.bukkit.plugin.java.JavaPlugin;
import org.tsl.tSLzone.commands.TSLzoneCommand;
import org.tsl.tSLzone.managers.ZoneManager;
import org.tsl.tSLzone.managers.ConfigManager;
import org.tsl.tSLzone.managers.MessageManager;

public final class TSLzone extends JavaPlugin {

    private static TSLzone instance;
    private ZoneManager zoneManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化消息管理器
        messageManager = new MessageManager(this);

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 初始化区域管理器
        zoneManager = new ZoneManager(this);

        // 注册命令和Tab补全
        TSLzoneCommand commandExecutor = new TSLzoneCommand(this);
        getCommand("tslzone").setExecutor(commandExecutor);
        getCommand("tslzone").setTabCompleter(commandExecutor);
        getCommand("tzone").setExecutor(commandExecutor);
        getCommand("tzone").setTabCompleter(commandExecutor);

        getLogger().info("TSLzone插件已启用！");
    }

    @Override
    public void onDisable() {
        if (zoneManager != null) {
            zoneManager.stopAllZones();
            zoneManager.saveData();
        }
        getLogger().info("TSLzone插件已禁用！");
    }

    public static TSLzone getInstance() {
        return instance;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
