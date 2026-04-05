package com.tinyplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class SleepManager implements Listener {
    private TinyPlus plugin;
    private Map<UUID, Boolean> underBlanket = new HashMap<>();
    private Map<String, UUID> bedOccupant = new HashMap<>();
    
    public SleepManager(TinyPlus plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        Player player = e.getPlayer();
        String bedKey = e.getBed().getLocation().toString();
        
        if (bedOccupant.containsKey(bedKey)) {
            Player partner = Bukkit.getPlayer(bedOccupant.get(bedKey));
            if (partner != null && partner.isOnline() && !partner.equals(player)) {
                e.setCancelled(true);
                openSleepMenu(player, partner);
                return;
            }
        }
        bedOccupant.put(bedKey, player.getUniqueId());
    }
    
    public void openSleepMenu(Player small, Player big) {
        String[] poses = plugin.getConfig().getStringList("sleep-poses").toArray(new String[0]);
        Component message = Component.text()
            .append(Component.text("§8╔══════════════════════════════════════════════════════════╗\n"))
            .append(Component.text("§l§b          🛌 ВЫБОР ПОЗЫ ДЛЯ СНА С §e§l" + big.getName() + "\n"))
            .append(Component.text("§8╠══════════════════════════════════════════════════════════╣\n"))
            .build();
        
        for (int i = 0; i < poses.length; i++) {
            Component button = Component.text("§8[§a" + (i+1) + "§8] §f" + poses[i])
                .hoverEvent(HoverEvent.showText(Component.text("§aНажми чтобы лечь в позе: " + poses[i])))
                .clickEvent(ClickEvent.runCommand("/sleeppose " + i + " " + big.getName()));
            message = message.append(button);
            if ((i + 1) % 3 == 0 || i == poses.length - 1) {
                message = message.append(Component.text("\n"));
            } else {
                message = message.append(Component.text("     "));
            }
        }
        message = message.append(Component.text("§8╚══════════════════════════════════════════════════════════╝"));
        small.sendMessage(message);
    }
    
    public void startSleeping(Player small, Player big, int poseId) {
        underBlanket.put(small.getUniqueId(), true);
        underBlanket.put(big.getUniqueId(), true);
        applySleepPosition(small, big, poseId);
        small.sendMessage(plugin.getConfig().getString("messages.under-blanket"));
        big.sendMessage(plugin.getConfig().getString("messages.under-blanket"));
        startSleepTimer(small, big);
    }
    
    private void applySleepPosition(Player small, Player big, int poseId) {
        Location l = big.getLocation();
        switch(poseId) {
            case 0: small.teleport(l.clone().add(0.5, 0, 0.5)); break;
            case 1: small.teleport(l.clone().add(0, 0.8, 0)); break;
            case 2: small.teleport(l.clone().add(-0.5, 0, 0.5)); break;
            case 3: small.teleport(l.clone().add(0.3, 0.2, 0.3)); break;
            case 4: small.teleport(l.clone().add(0.4, 0, 0.6)); break;
            case 5: small.teleport(l.clone().add(0.6, 0.1, -0.4)); break;
            case 6: small.teleport(l.clone().add(0.2, 0.4, 0.3)); break;
            case 7: small.teleport(l.clone().add(0, 0.1, 0.8)); break;
            case 8: small.teleport(l.clone().add(0.3, 0.2, -0.2)); break;
            default: small.teleport(l.clone().add(0.5, 0, 0.5));
        }
    }
    
    private void startSleepTimer(Player small, Player big) {
        int seconds = plugin.getConfig().getInt("sleep-duration-seconds", 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (small.isOnline() && big.isOnline() && small.getWorld().getTime() >= 12500) {
                    small.getWorld().setTime(0);
                    small.sendMessage(plugin.getConfig().getString("messages.morning"));
                    big.sendMessage(plugin.getConfig().getString("messages.morning"));
                }
                stopSleeping(small, big);
            }
        }.runTaskLater(plugin, 20L * seconds);
    }
    
    public void escapeBlanket(Player p) { 
        underBlanket.remove(p.getUniqueId()); 
        p.sendMessage(plugin.getConfig().getString("messages.blanket-escape"));
    }
    
    public boolean isUnderBlanket(Player p) { return underBlanket.containsKey(p.getUniqueId()); }
    
    public void stopSleeping(Player small, Player big) { 
        underBlanket.remove(small.getUniqueId()); 
        underBlanket.remove(big.getUniqueId()); 
    }
    
    public void onQuit(Player p) { underBlanket.remove(p.getUniqueId()); }
}
