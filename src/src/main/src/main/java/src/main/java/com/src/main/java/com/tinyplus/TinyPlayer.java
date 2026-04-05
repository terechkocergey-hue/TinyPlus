// TinyPlayer.java
package com.tinyplus;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import java.util.*;

public class TinyPlayer {
    private TinyPlus plugin;
    private Set<UUID> smallPlayers = new HashSet<>();
    
    public TinyPlayer(TinyPlus plugin) { this.plugin = plugin; }
    
    public void setSmall(Player p, boolean small) {
        UUID id = p.getUniqueId();
        if (small) {
            smallPlayers.add(id);
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(plugin.getConfig().getDouble("small-scale"));
            p.sendMessage(plugin.getConfig().getString("messages.became-small"));
            p.setCustomName("§7[3px] " + p.getName());
            p.setCustomNameVisible(true);
        } else {
            smallPlayers.remove(id);
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);
            p.sendMessage(plugin.getConfig().getString("messages.became-normal"));
            p.setCustomName(p.getName());
        }
    }
    public boolean isSmall(Player p) { return smallPlayers.contains(p.getUniqueId()); }
}
