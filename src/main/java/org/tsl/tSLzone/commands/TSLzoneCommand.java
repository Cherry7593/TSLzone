package org.tsl.tSLzone.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.tsl.tSLzone.TSLzone;
import org.tsl.tSLzone.models.Zone;
import org.tsl.tSLzone.managers.MessageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TSLzoneCommand implements CommandExecutor, TabCompleter {
    private final TSLzone plugin;
    private final MessageManager messageManager;

    public TSLzoneCommand(TSLzone plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tslzone.use")) {
            sender.sendMessage(messageManager.getNoPermission());
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "pos1":
                return handlePos1(sender);
            case "pos2":
                return handlePos2(sender);
            case "create":
                return handleCreate(sender, args);
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handlePos1(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getPlayerOnly());
            return true;
        }

        Location location = player.getLocation();
        plugin.getZoneManager().setPlayerPos1(player.getName(), location);

        player.sendMessage(messageManager.getPos1Set(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        ));
        return true;
    }

    private boolean handlePos2(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getPlayerOnly());
            return true;
        }

        Location location = player.getLocation();
        plugin.getZoneManager().setPlayerPos2(player.getName(), location);

        player.sendMessage(messageManager.getPos2Set(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        ));
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getNoAdminPermission());
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getPlayerOnly());
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(messageManager.getUsageCreate());
            return true;
        }

        String zoneName = args[1];
        double damage;
        int frequency;

        try {
            damage = Double.parseDouble(args[2]);
            frequency = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messageManager.getMessage("zone.create.invalid-numbers"));
            return true;
        }

        if (damage <= 0) {
            sender.sendMessage(messageManager.getMessage("zone.create.invalid-damage"));
            return true;
        }

        if (frequency <= 0) {
            sender.sendMessage(messageManager.getMessage("zone.create.invalid-frequency"));
            return true;
        }

        boolean success = plugin.getZoneManager().createZone(zoneName, player.getName(), damage, frequency);

        if (success) {
            player.sendMessage(messageManager.getZoneCreateSuccess(zoneName));
            player.sendMessage(messageManager.getZoneCreateInfo(damage, frequency));
            player.sendMessage(messageManager.getZoneCreateTip(zoneName));
        } else {
            player.sendMessage(messageManager.getMessage("zone.create.failed"));
        }

        return true;
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getNoAdminPermission());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messageManager.getUsageStart());
            return true;
        }

        String zoneName = args[1];
        boolean success = plugin.getZoneManager().startZone(zoneName);

        if (success) {
            sender.sendMessage(messageManager.getZoneStartSuccess(zoneName));
        } else {
            sender.sendMessage(messageManager.getMessage("zone.start.failed"));
        }

        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getNoAdminPermission());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messageManager.getUsageStop());
            return true;
        }

        String zoneName = args[1];
        boolean success = plugin.getZoneManager().stopZone(zoneName);

        if (success) {
            sender.sendMessage(messageManager.getZoneStopSuccess(zoneName));
        } else {
            sender.sendMessage(messageManager.getMessage("zone.stop.failed"));
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getNoAdminPermission());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messageManager.getUsageRemove());
            return true;
        }

        String zoneName = args[1];
        boolean success = plugin.getZoneManager().removeZone(zoneName);

        if (success) {
            sender.sendMessage(messageManager.getZoneRemoveSuccess(zoneName));
        } else {
            sender.sendMessage(messageManager.getMessage("zone.remove.failed"));
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        Map<String, Zone> zones = plugin.getZoneManager().getAllZones();

        if (zones.isEmpty()) {
            sender.sendMessage(messageManager.getMessage("zone.list.empty"));
            return true;
        }

        sender.sendMessage(messageManager.getMessage("zone.list.header"));

        for (Zone zone : zones.values()) {
            String status = zone.isActive() ?
                messageManager.getMessageWithoutPrefix("zone.list.status-active") :
                messageManager.getMessageWithoutPrefix("zone.list.status-inactive");

            String pos1Str = String.format("(%.1f, %.1f, %.1f)",
                zone.getPos1().getX(), zone.getPos1().getY(), zone.getPos1().getZ());
            String pos2Str = String.format("(%.1f, %.1f, %.1f)",
                zone.getPos2().getX(), zone.getPos2().getY(), zone.getPos2().getZ());

            sender.sendMessage(messageManager.getMessage("zone.list.entry-name", zone.getName()));
            sender.sendMessage(messageManager.getMessage("zone.list.entry-positions", pos1Str, pos2Str));
            sender.sendMessage(messageManager.getMessage("zone.list.entry-settings", zone.getDamage(), zone.getFrequency()));
            sender.sendMessage(messageManager.getMessage("zone.list.entry-status", status));
            sender.sendMessage("");
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getNoAdminPermission());
            return true;
        }

        plugin.getConfigManager().reloadConfig();
        plugin.getMessageManager().reloadMessages();
        plugin.getZoneManager().reloadZones();

        sender.sendMessage(messageManager.getConfigReloadSuccess());
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messageManager.getMessage("help.header"));
        sender.sendMessage(messageManager.getMessage("help.pos1"));
        sender.sendMessage(messageManager.getMessage("help.pos2"));

        if (sender.hasPermission("tslzone.admin")) {
            sender.sendMessage(messageManager.getMessage("help.create"));
            sender.sendMessage(messageManager.getMessage("help.start"));
            sender.sendMessage(messageManager.getMessage("help.stop"));
            sender.sendMessage(messageManager.getMessage("help.remove"));
            sender.sendMessage(messageManager.getMessage("help.reload"));
        }

        sender.sendMessage(messageManager.getMessage("help.list"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("pos1", "pos2", "list"));

            if (sender.hasPermission("tslzone.admin")) {
                completions.addAll(Arrays.asList("create", "start", "stop", "remove", "reload"));
            }

            return completions;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("start") || subCommand.equals("stop") || subCommand.equals("remove")) {
                return new ArrayList<>(plugin.getZoneManager().getAllZones().keySet());
            }
        }

        return new ArrayList<>();
    }
}
