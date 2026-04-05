package com.tinyplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class AnimationManager {
    
    private TinyPlus plugin;
    private String[] animations;
    
    public AnimationManager(TinyPlus plugin) {
        this.plugin = plugin;
        this.animations = plugin.getConfig().getStringList("animations").toArray(new String[0]);
    }
    
    public void openMenu(Player big, Player small) {
        Component.Builder builder = Component.text();
        builder.append(Component.text("§8╔══════════════════════════════════════════════════════════╗\n"));
        builder.append(Component.text("§l§d          🎭 АНИМАЦИИ С §e§l" + small.getName() + "\n"));
        builder.append(Component.text("§8╠══════════════════════════════════════════════════════════╣\n"));
        
        for (int i = 0; i < animations.length; i++) {
            Component button = Component.text("§8[§a" + (i+1) + "§8] §f" + animations[i])
                .hoverEvent(HoverEvent.showText(Component.text("§aНажми чтобы " + animations[i])))
                .clickEvent(ClickEvent.runCommand("/anim " + (i+1) + " " + small.getName()));
            builder.append(button);
            if ((i + 1) % 3 == 0 || i == animations.length - 1) {
                builder.append(Component.text("\n"));
            } else {
                builder.append(Component.text("     "));
            }
        }
        builder.append(Component.text("§8╚══════════════════════════════════════════════════════════╝"));
        big.sendMessage(builder.build());
    }
    
    public void playAnimation(Player big, Player small, int animId) {
        if (animId < 1 || animId > animations.length) return;
        String animation = animations[animId - 1];
        
        switch(animation) {
            case "Погладить по голове":
                animatePat(big, small);
                break;
            case "Пощекотать":
                animateTickle(big, small);
                break;
            case "Покружить":
                animateSpin(big, small);
                break;
            case "Подбросить":
                animateThrow(big, small);
                break;
            case "Укачивать":
                animateRock(big, small);
                break;
            case "Поцеловать в губы":
                animateKiss(big, small);
                break;
        }
    }
    
    private void animatePat(Player big, Player small) {
        big.swingMainHand();
        small.sendTitle("§e✨ *Погладили по голове* ✨", "§7Мур~ мур~", 5, 30, 5);
        big.playSound(small.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
        small.playSound(small.getLocation(), Sound.ENTITY_CAT_PURR, 1, 1);
        Location head = small.getLocation().add(0, 0.8, 0);
        small.getWorld().spawnParticle(org.bukkit.Particle.HEART, head, 5, 0.3, 0.3, 0.3);
    }
    
    private void animateTickle(Player big, Player small) {
        big.swingMainHand();
        small.sendTitle("§c😆 *Щекотно!* 😆", "§7Хи-хи-хи!", 5, 30, 5);
        small.playSound(small.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
        small.setVelocity(new Vector(0, 0.2, 0));
        small.getWorld().spawnParticle(org.bukkit.Particle.NOTE, small.getLocation(), 10, 0.2, 0.5, 0.2);
    }
    
    private void animateSpin(Player big, Player small) {
        big.swingMainHand();
        small.sendTitle("§d🔄 *Кружится* 🔄", "§7Ух-ты!", 5, 30, 5);
        small.playSound(small.getLocation(), Sound.ENTITY_PLAYER_SWIM, 1, 1);
        
        Location center = big.getLocation().clone();
        Location startLoc = small.getLocation().clone();
        
        new BukkitRunnable() {
            int angle = 0;
            @Override
            public void run() {
                if (angle < 360) {
                    double rad = Math.toRadians(angle);
                    double x = center.getX() + Math.cos(rad) * 1.2;
                    double z = center.getZ() + Math.sin(rad) * 1.2;
                    small.teleport(new Location(small.getWorld(), x, center.getY(), z, angle, 0));
                    angle += 30;
                    small.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, small.getLocation(), 2);
                } else {
                    small.teleport(startLoc);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void animateThrow(Player big, Player small) {
        big.swingMainHand();
        small.sendTitle("§6🚀 *Подбросили!* 🚀", "§7Вжух!", 5, 30, 5);
        small.playSound(small.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
        
        Location start = small.getLocation().clone();
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick < 12) {
                    small.teleport(start.clone().add(0, 0.35 * tick, 0));
                    tick++;
                } else {
                    small.teleport(start);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void animateRock(Player big, Player small) {
        big.swingMainHand();
        small.sendTitle("§3🎵 *Укачивают* 🎵", "§7Баю-бай...", 5, 50, 5);
        small.playSound(small.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        
        Location start = small.getLocation().clone();
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick < 20) {
                    double offset = Math.sin(tick * 0.5) * 0.1;
                    small.teleport(start.clone().add(offset, 0, 0));
                    tick++;
                } else {
                    small.teleport(start);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void animateKiss(Player big, Player small) {
        big.swingMainHand();
        small.swingMainHand();
        Location face = small.getLocation().add(0, 0.5, 0);
        small.getWorld().spawnParticle(org.bukkit.Particle.HEART, face, 15, 0.3, 0.3, 0.3);
        small.playSound(small.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        big.playSound(big.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        small.getWorld().spawnParticle(org.bukkit.Particle.GLOW, face, 20, 0.2, 0.2, 0.2);
        small.sendTitle("§d💋 *Чмок!* 💋", "§7Тебя поцеловали", 5, 30, 5);
        big.sendTitle("§d💋 *Чмок!* 💋", "§7Ты поцеловал " + small.getName(), 5, 30, 5);
        small.setVelocity(new Vector(0, 0.1, -0.2));
    }
}
