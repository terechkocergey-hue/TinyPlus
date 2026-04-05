package com.tinyplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class CarryManager {
    private TinyPlus plugin;
    private Map<UUID, UUID> carrying = new HashMap<>();
    private Map<UUID, Integer> carryPose = new HashMap<>();
    private String[] poses;
    
    public CarryManager(TinyPlus plugin) {
        this.plugin = plugin;
        this.poses = plugin.getConfig().getStringList("carry-poses").toArray(new String[0]);
        startCarryUpdateTask();
    }
    
    public void pickUp(Player big, Player small) {
        UUID bigId = big.getUniqueId();
        UUID smallId = small.getUniqueId();
        
        if (carrying.containsKey(bigId)) drop(big);
        
        carrying.put(bigId, smallId);
        small.setInvisible(true);
        small.setInvulnerable(true);
        
        big.sendMessage(plugin.getConfig().getString("messages.picked-up").replace("{player}", small.getName()));
        small.sendMessage(plugin.getConfig().getString("messages.was-picked-up").replace("{player}", big.getName()));
        
        openCarryMenu(big, small);
    }
    
    public void openCarryMenu(Player big, Player small) {
        Component message = Component.text()
            .append(Component.text("§8╔══════════════════════════════════════════════════════════╗\n"))
            .append(Component.text("§l§6          🤲 ВЫБОР ПОЗЫ ПЕРЕНОСА ДЛЯ §e§l" + small.getName() + "\n"))
            .append(Component.text("§8╠══════════════════════════════════════════════════════════╣\n"))
            .build();
        
        for (int i = 0; i < poses.length; i++) {
            Component button = Component.text("§8[§a" + (i+1) + "§8] §f" + poses[i])
                .hoverEvent(HoverEvent.showText(Component.text("§aНажми чтобы посадить на: " + poses[i])))
                .clickEvent(ClickEvent.runCommand("/carrypose " + i + " " + small.getName()));
            message = message.append(button);
            if ((i + 1) % 3 == 0 || i == poses.length - 1) {
                message = message.append(Component.text("\n"));
            } else {
                message = message.append(Component.text("     "));
            }
        }
        message = message.append(Component.text("§8╚══════════════════════════════════════════════════════════╝"));
        big.sendMessage(message);
    }
    
    public void setCarryPose(Player big, Player small, int poseId) {
        if (poseId < 0 || poseId >= poses.length) return;
        carryPose.put(big.getUniqueId(), poseId);
        updateCarryPosition(big, small, poseId);
        big.sendMessage("§aПоза изменена: " + poses[poseId]);
    }
    
    private void updateCarryPosition(Player big, Player small, int poseId) {
        switch(poseId) {
            case 0: small.teleport(big.getLocation().add(0, 1.8, 0)); break;
            case 1: small.teleport(big.getLocation().add(0.3, 0.8, 0)); break;
            case 2: small.teleport(big.getLocation().add(0.5, 1.2, 0)); break;
            case 3: small.teleport(big.getLocation().add(0, 1.0, 0.5)); break;
            case 4: small.teleport(big.getLocation().add(0, 0.9, 0.2)); break;
            case 5: small.teleport(big.getLocation().add(0, 0.3, 0.4)); break;
            case 6: small.teleport(big.getLocation().add(0, 0.7, -0.3)); break;
            default: small.teleport(big.getLocation().add(0, 1.2, 0));
        }
    }
    
    private void startCarryUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, UUID> entry : carrying.entrySet()) {
                    Player big = Bukkit.getPlayer(entry.getKey());
                    Player small = Bukkit.getPlayer(entry.getValue());
                    if (big != null && small != null && big.isOnline() && small.isOnline()) {
                        int pose = carryPose.getOrDefault(entry.getKey(), 3);
                        updateCarryPosition(big, small, pose);
                    } else if (big != null) {
                        drop(big);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    public void drop(Player big) {
        UUID bigId = big.getUniqueId();
        if (!carrying.containsKey(bigId)) return;
        
        Player small = Bukkit.getPlayer(carrying.get(bigId));
        if (small != null) {
            small.setInvisible(false);
            small.setInvulnerable(false);
            small.teleport(big.getLocation().add(0, 1, 0));
            small.sendMessage(plugin.getConfig().getString("messages.dropped"));
        }
        carrying.remove(bigId);
        carryPose.remove(bigId);
    }
    
    public boolean isCarrying(Player p) { return carrying.containsKey(p.getUniqueId()); }
    public void onQuit(Player p) { drop(p); }
}
