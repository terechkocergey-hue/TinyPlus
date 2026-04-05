package com.tinyplus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public class PoseManager {
    private TinyPlus plugin;
    private Set<UUID> sitting = new HashSet<>();
    private Set<UUID> lying = new HashSet<>();
    private Map<UUID, UUID> sittingOnSmall = new HashMap<>();
    
    public PoseManager(TinyPlus plugin) { this.plugin = plugin; }
    
    public void toggleSit(Player p) {
        UUID id = p.getUniqueId();
        if (sitting.contains(id)) {
            sitting.remove(id);
        } else {
            sitting.add(id);
            lying.remove(id);
        }
        p.sendMessage(plugin.getConfig().getString("messages.now-sitting"));
    }
    
    public void toggleLie(Player p) {
        UUID id = p.getUniqueId();
        if (lying.contains(id)) {
            lying.remove(id);
        } else {
            lying.add(id);
            sitting.remove(id);
        }
        p.sendMessage(plugin.getConfig().getString("messages.now-lying"));
    }
    
    public void sitOnSmall(Player big, Player small) {
        if (!plugin.getTinyPlayer().isSmall(small)) return;
        if (sittingOnSmall.containsKey(small.getUniqueId())) {
            big.sendMessage("§cНа этом маленьком уже кто-то сидит!");
            return;
        }
        sittingOnSmall.put(big.getUniqueId(), small.getUniqueId());
        sittingOnSmall.put(small.getUniqueId(), big.getUniqueId());
        big.teleport(small.getLocation().add(0, 0.8, 0));
        big.setInvulnerable(true);
        small.setInvulnerable(true);
        big.sendMessage(plugin.getConfig().getString("messages.sitting-on").replace("{big}", big.getName()).replace("{small}", small.getName()));
        small.sendMessage("§d" + big.getName() + " сел на тебя!");
    }
    
    public void standUp(Player big) {
        UUID bigId = big.getUniqueId();
        if (sittingOnSmall.containsKey(bigId)) {
            UUID smallId = sittingOnSmall.get(bigId);
            Player small = Bukkit.getPlayer(smallId);
            big.setInvulnerable(false);
            if (small != null) small.setInvulnerable(false);
            sittingOnSmall.remove(bigId);
            sittingOnSmall.remove(smallId);
            big.sendMessage("§aТы слез с маленького");
            if (small != null) small.sendMessage("§a" + big.getName() + " слез с тебя");
        }
    }
    
    public void onQuit(Player p) { 
        sitting.remove(p.getUniqueId()); 
        lying.remove(p.getUniqueId());
        if (sittingOnSmall.containsKey(p.getUniqueId())) {
            Player other = Bukkit.getPlayer(sittingOnSmall.get(p.getUniqueId()));
            if (other != null) other.setInvulnerable(false);
            sittingOnSmall.remove(p.getUniqueId());
        }
    }
}
