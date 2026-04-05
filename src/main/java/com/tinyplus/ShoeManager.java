package com.tinyplus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class ShoeManager {
    private TinyPlus plugin;
    private Map<UUID, UUID> inShoe = new HashMap<>();
    
    public ShoeManager(TinyPlus plugin) { this.plugin = plugin; startShoeUpdateTask(); }
    
    public void shoveIntoShoe(Player big, Player small) {
        inShoe.put(small.getUniqueId(), big.getUniqueId());
        // НЕ делаем невидимым
        small.setInvulnerable(true);
        small.setAllowFlight(true);
        small.setFlying(true);
        big.sendMessage(plugin.getConfig().getString("messages.shoved-in-shoe").replace("{big}", big.getName()).replace("{small}", small.getName()));
        small.sendMessage("§7👟 Тебя засунули в кроссовок! Нажми X чтобы вылезти");
    }
    
    public void removeFromShoe(Player small) {
        inShoe.remove(small.getUniqueId());
        small.setInvulnerable(false);
        small.setAllowFlight(false);
        small.setFlying(false);
        small.sendMessage(plugin.getConfig().getString("messages.escaped-shoe"));
        small.teleport(small.getLocation().add(0, 1, 0));
    }
    
    private void startShoeUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, UUID> entry : inShoe.entrySet()) {
                    Player small = Bukkit.getPlayer(entry.getKey());
                    Player big = Bukkit.getPlayer(entry.getValue());
                    if (small != null && big != null && big.isOnline()) {
                        small.teleport(big.getLocation().add(0, 0.2, 0));
                        small.setVelocity(new org.bukkit.util.Vector(0, 0, 0)); // Убираем тряску
                    } else if (small != null) {
                        removeFromShoe(small);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Увеличил интервал
    }
    
    public boolean isInShoe(Player p) { return inShoe.containsKey(p.getUniqueId()); }
    public void onQuit(Player p) { removeFromShoe(p); }
}
