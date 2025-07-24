package org.tsl.tSLzone.models;

import org.bukkit.Location;
import org.bukkit.World;

public class Zone {
    private String name;
    private Location pos1;
    private Location pos2;
    private double damage;
    private int frequency; // 伤害频率，单位秒
    private boolean active;
    private int taskId; // 用于存储任务ID

    public Zone(String name, Location pos1, Location pos2, double damage, int frequency) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.damage = damage;
        this.frequency = frequency;
        this.active = false;
        this.taskId = -1;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    // 检查位置是否在区域内
    public boolean isInZone(Location location) {
        if (pos1 == null || pos2 == null || location == null) {
            return false;
        }

        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    // 获取区域大小
    public int getSize() {
        if (pos1 == null || pos2 == null) {
            return 0;
        }

        int sizeX = Math.abs((int)(pos1.getX() - pos2.getX())) + 1;
        int sizeY = Math.abs((int)(pos1.getY() - pos2.getY())) + 1;
        int sizeZ = Math.abs((int)(pos1.getZ() - pos2.getZ())) + 1;

        return sizeX * sizeY * sizeZ;
    }

    // 获取区域中心点
    public Location getCenter() {
        if (pos1 == null || pos2 == null) {
            return null;
        }

        double centerX = (pos1.getX() + pos2.getX()) / 2;
        double centerY = (pos1.getY() + pos2.getY()) / 2;
        double centerZ = (pos1.getZ() + pos2.getZ()) / 2;

        return new Location(pos1.getWorld(), centerX, centerY, centerZ);
    }
}
