package com.tinyplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class TinyPlus extends JavaPlugin implements Listener {

    private static TinyPlus instance;
    private Set<UUID> smallPlayers = new HashSet<>();
    private Map<UUID, UUID> carrying = new HashMap<>();
    private Map<UUID, String> location = new HashMap<>(); // "hand", "pocket", "shoe"
    private Map<UUID, UUID> pendingSleep = new HashMap<>(); // маленький -> большой
    private Map<UUID, Long> sleepTime = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // ==================== КОМАНДЫ УМЕНЬШЕНИЯ ====================
        
        getCommand("smoll").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                smallPlayers.add(target.getUniqueId());
                target.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(getConfig().getDouble("small-scale", 0.35));
                target.sendMessage("§a✨ Ты уменьшился до 3 пикселей! ✨");
                p.sendMessage("§aТы уменьшил " + target.getName());
            }
            return true;
        });

        getCommand("unsmoll").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) {
                if (location.containsKey(p.getUniqueId())) {
                    location.remove(p.getUniqueId());
                    p.setInvulnerable(false);
                    p.setWalkSpeed(0.2f);
                    p.teleport(p.getLocation().add(0, 1, 0));
                    p.sendMessage("§c📌 Тебя опустили на землю");
                }
                smallPlayers.remove(p.getUniqueId());
                p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);
                p.sendMessage("§c📏 Ты вернул нормальный размер");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                smallPlayers.remove(target.getUniqueId());
                target.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.0);
                target.sendMessage("§c📏 Ты вернул нормальный размер");
                p.sendMessage("§aТы вернул размер " + target.getName());
            }
            return true;
        });

        // ==================== КОМАНДЫ ПЕРЕНОСА ====================
        
        getCommand("carry").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length == 0) return false;
            Player small = Bukkit.getPlayer(args[0]);
            if (small != null && smallPlayers.contains(small.getUniqueId())) {
                carrying.put(big.getUniqueId(), small.getUniqueId());
                location.put(small.getUniqueId(), "hand");
                small.setInvulnerable(true);
                small.setWalkSpeed(0);
                big.sendMessage("§6🤲 Ты взял на руки " + small.getName());
                small.sendMessage("§6🫂 Тебя взял на руки " + big.getName());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (location.containsKey(small.getUniqueId()) && "hand".equals(location.get(small.getUniqueId()))) {
                            small.teleport(big.getLocation().add(0, 1.2, 0));
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this, 0L, 5L);
            } else {
                big.sendMessage("§cИгрок не найден или не маленький!");
            }
            return true;
        });

        getCommand("pocket").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length == 0) return false;
            Player small = Bukkit.getPlayer(args[0]);
            if (small != null && smallPlayers.contains(small.getUniqueId())) {
                location.put(small.getUniqueId(), "pocket");
                small.setInvulnerable(true);
                small.setWalkSpeed(0);
                small.sendMessage("§e👖 Тебя положили в карман! Используй /unsmoll чтобы вылезти");
                big.sendMessage("§eТы положил " + small.getName() + " в карман!");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (location.containsKey(small.getUniqueId()) && "pocket".equals(location.get(small.getUniqueId()))) {
                            small.teleport(big.getLocation().add(0.3, 0.6, 0));
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this, 0L, 5L);
            }
            return true;
        });

        getCommand("shoe").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length == 0) return false;
            Player small = Bukkit.getPlayer(args[0]);
            if (small != null && smallPlayers.contains(small.getUniqueId())) {
                location.put(small.getUniqueId(), "shoe");
                small.setInvulnerable(true);
                small.setWalkSpeed(0);
                small.sendMessage("§7👟 Тебя засунули в кроссовок! Используй /unsmoll чтобы вылезти");
                big.sendMessage("§7Ты засунул " + small.getName() + " в кроссовок!");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (location.containsKey(small.getUniqueId()) && "shoe".equals(location.get(small.getUniqueId()))) {
                            small.teleport(big.getLocation().add(0, 0.2, 0));
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this, 0L, 5L);
            }
            return true;
        });

        getCommand("drop").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            UUID bigId = big.getUniqueId();
            if (carrying.containsKey(bigId)) {
                Player small = Bukkit.getPlayer(carrying.get(bigId));
                if (small != null) {
                    location.remove(small.getUniqueId());
                    small.setInvulnerable(false);
                    small.setWalkSpeed(0.2f);
                    small.teleport(big.getLocation().add(0, 1, 0));
                    small.sendMessage("§c📌 Тебя опустили на землю");
                }
                carrying.remove(bigId);
                big.sendMessage("§cТы опустил игрока");
            } else {
                big.sendMessage("§cТы никого не несёшь!");
            }
            return true;
        });

        // ==================== КОМАНДЫ ПОЗ ====================
        
        getCommand("sit").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) {
                p.sendMessage("§e💺 Ты сел");
                p.setWalkSpeed(0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setWalkSpeed(0.2f);
                    }
                }.runTaskLater(this, 60L);
            }
            return true;
        });

        getCommand("lie").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) {
                p.sendMessage("§7🛌 Ты лёг");
                p.setWalkSpeed(0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setWalkSpeed(0.2f);
                    }
                }.runTaskLater(this, 100L);
            }
            return true;
        });

        getCommand("stand").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) {
                p.setWalkSpeed(0.2f);
                p.sendMessage("§aТы встал");
            }
            return true;
        });

        // ==================== КОМАНДЫ АНИМАЦИЙ ====================
        
        getCommand("anim").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length < 2) return false;
            Player small = Bukkit.getPlayer(args[0]);
            if (small == null) {
                big.sendMessage("§cИгрок не найден!");
                return true;
            }
            try {
                int animId = Integer.parseInt(args[1]);
                switch (animId) {
                    case 1:
                        small.sendTitle("§e✨ *Погладили по голове* ✨", "§7Мур~", 5, 30, 5);
                        small.playSound(small.getLocation(), Sound.ENTITY_CAT_PURR, 1, 1);
                        small.getWorld().spawnParticle(org.bukkit.Particle.HEART, small.getLocation().add(0, 0.8, 0), 10, 0.3, 0.3, 0.3);
                        big.sendMessage("§aТы погладил " + small.getName());
                        break;
                    case 2:
                        small.sendTitle("§d💋 *Чмок!* 💋", "§7Тебя поцеловали", 5, 30, 5);
                        small.getWorld().spawnParticle(org.bukkit.Particle.HEART, small.getLocation().add(0, 0.5, 0), 20, 0.3, 0.3, 0.3);
                        small.playSound(small.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        big.sendMessage("§dТы поцеловал " + small.getName());
                        break;
                    case 3:
                        small.sendTitle("§6🚀 *Подбросили!* 🚀", "§7Вжух!", 5, 30, 5);
                        small.setVelocity(new Vector(0, 1, 0));
                        small.playSound(small.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
                        big.sendMessage("§6Ты подбросил " + small.getName());
                        break;
                    default:
                        big.sendMessage("§cАнимации: 1-погладить, 2-поцеловать, 3-подбросить");
                }
            } catch (NumberFormatException e) {
                big.sendMessage("§cИспользуй: /anim <ник> <1-3>");
            }
            return true;
        });

        // ==================== КОМАНДЫ ДЛЯ СНА ====================
        
        getCommand("pose").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player small)) return true;
            if (args.length == 0) return false;
            if (!pendingSleep.containsKey(small.getUniqueId())) {
                small.sendMessage("§cТы не пытаешься лечь спать! Сначала ложись в кровать с большим игроком.");
                return true;
            }
            try {
                int poseId = Integer.parseInt(args[0]);
                if (poseId < 1 || poseId > 5) {
                    small.sendMessage("§cВыбери позу от 1 до 5!");
                    return true;
                }
                Player big = Bukkit.getPlayer(pendingSleep.get(small.getUniqueId()));
                if (big == null || !big.isOnline()) {
                    small.sendMessage("§cБольшой игрок больше не в сети!");
                    pendingSleep.remove(small.getUniqueId());
                    return true;
                }
                
                // Применяем позу
                Location loc = big.getLocation();
                switch(poseId) {
                    case 1: small.teleport(loc.clone().add(0.5, 0, 0.5)); break;
                    case 2: small.teleport(loc.clone().add(0, 0.8, 0)); break;
                    case 3: small.teleport(loc.clone().add(-0.5, 0, 0.5)); break;
                    case 4: small.teleport(loc.clone().add(0.3, 0.2, 0.3)); break;
                    case 5: small.teleport(loc.clone().add(0.4, 0, 0.6)); break;
                }
                
                small.sendMessage("§b🛏️ Вы под одеялом!");
                big.sendMessage("§b🛏️ Вы под одеялом!");
                
                // Запускаем таймер сна
                int seconds = getConfig().getInt("sleep-duration-seconds", 60);
                sleepTime.put(small.getUniqueId(), System.currentTimeMillis());
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (sleepTime.containsKey(small.getUniqueId())) {
                            small.getWorld().setTime(0);
                            small.sendMessage("§a🌅 Доброе утро!");
                            big.sendMessage("§a🌅 Доброе утро!");
                            sleepTime.remove(small.getUniqueId());
                            pendingSleep.remove(small.getUniqueId());
                        }
                    }
                }.runTaskLater(this, 20L * seconds);
                
            } catch (NumberFormatException e) {
                small.sendMessage("§cИспользуй: /pose <номер> (1-5)");
            }
            return true;
        });

        getCommand("visible").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) {
                p.setInvisible(false);
                p.sendMessage("§a✅ Теперь ты видим!");
            }
            return true;
        });

        // ActionBar таск
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (location.containsKey(p.getUniqueId())) {
                        String loc = location.get(p.getUniqueId());
                        switch (loc) {
                            case "hand":
                                p.sendActionBar("§6🤲 Ты на руках! Используй /unsmoll чтобы слезть");
                                break;
                            case "pocket":
                                p.sendActionBar("§e👖 Ты в кармане! Используй /unsmoll чтобы вылезти");
                                break;
                            case "shoe":
                                p.sendActionBar("§7👟 Ты в кроссовке! Используй /unsmoll чтобы вылезти");
                                break;
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        getLogger().info("§aTinyPlus Ultimate включён! Команда /pose для выбора позы сна.");
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        Player player = e.getPlayer();
        
        // Если игрок уже маленький
        if (smallPlayers.contains(player.getUniqueId())) {
            // Ищем рядом большого игрока
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!smallPlayers.contains(p.getUniqueId()) && p.getLocation().distance(player.getLocation()) < 3) {
                    e.setCancelled(true);
                    
                    // Открываем меню выбора позы
                    String[] poses = {
                        "§fКалачиком у большого",
                        "§fСверху на животе",
                        "§fСпина к спине",
                        "§fВ обнимку",
                        "§fЛожкой"
                    };
                    
                    player.sendMessage("§8╔══════════════════════════════════════╗");
                    player.sendMessage("§l§b          🛌 ВЫБОР ПОЗЫ ДЛЯ СНА");
                    player.sendMessage("§8╠══════════════════════════════════════╣");
                    
                    for (int i = 0; i < poses.length; i++) {
                        player.sendMessage("§8[§a" + (i+1) + "§8] " + poses[i]);
                    }
                    
                    player.sendMessage("§8╚══════════════════════════════════════╝");
                    player.sendMessage("§7Введи §e/pose <номер> §7чтобы выбрать позу");
                    
                    pendingSleep.put(player.getUniqueId(), p.getUniqueId());
                    player.sendMessage("§eТы ложишься спать с " + p.getName());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        smallPlayers.remove(p.getUniqueId());
        location.remove(p.getUniqueId());
        sleepTime.remove(p.getUniqueId());
        pendingSleep.remove(p.getUniqueId());
        carrying.values().remove(p.getUniqueId());
    }

    public static TinyPlus getInstance() { return instance; }
}
