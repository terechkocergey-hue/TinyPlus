package com.tinyplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TinyPlus extends JavaPlugin implements Listener {
    
    private static TinyPlus instance;
    private TinyPlayer tinyPlayer;
    private PoseManager poseManager;
    private CarryManager carryManager;
    private SleepManager sleepManager;
    private AnimationManager animationManager;
    private ShoeManager shoeManager;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        tinyPlayer = new TinyPlayer(this);
        poseManager = new PoseManager(this);
        carryManager = new CarryManager(this);
        sleepManager = new SleepManager(this);
        animationManager = new AnimationManager(this);
        shoeManager = new ShoeManager(this);
        
        getServer().getPluginManager().registerEvents(this, this);
        
        // Команда /smoll
        getCommand("smoll").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) tinyPlayer.setSmall(target, true);
            return true;
        });
        
        // Команда /unsmoll
        getCommand("unsmoll").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) tinyPlayer.setSmall(target, false);
            return true;
        });
        
        // Команда /stand (слезть с маленького)
        getCommand("stand").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) poseManager.standUp(p);
            return true;
        });
        
        // НОВАЯ КОМАНДА /sit
        getCommand("sit").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) poseManager.toggleSit(p);
            return true;
        });
        
        // НОВАЯ КОМАНДА /lie
        getCommand("lie").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) poseManager.toggleLie(p);
            return true;
        });
        
        // НОВАЯ КОМАНДА /carry (взять ближайшего маленького)
        getCommand("carry").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            Player target = big.getWorld().getPlayers().stream()
                .filter(p -> tinyPlayer.isSmall(p) && p.getLocation().distance(big.getLocation()) < 3)
                .findFirst().orElse(null);
            if (target != null) {
                carryManager.pickUp(big, target);
            } else {
                big.sendMessage("§cРядом нет маленького игрока!");
            }
            return true;
        });
        
        // НОВАЯ КОМАНДА /drop (опустить маленького)
        getCommand("drop").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            carryManager.drop(big);
            return true;
        });
        
        // НОВАЯ КОМАНДА /poses (меню поз переноса)
        getCommand("poses").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (carryManager.isCarrying(big)) {
                Player small = carryManager.getCarriedPlayer(big);
                if (small != null) carryManager.openCarryMenu(big, small);
            } else {
                big.sendMessage("§cТы никого не несёшь!");
            }
            return true;
        });
        
        // НОВАЯ КОМАНДА /animlist (меню анимаций)
        getCommand("animlist").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length == 0) {
                big.sendMessage("§cИспользуй: /animlist <ник>");
                return true;
            }
            Player small = Bukkit.getPlayer(args[0]);
            if (small != null && tinyPlayer.isSmall(small)) {
                animationManager.openMenu(big, small);
            } else {
                big.sendMessage("§cИгрок не найден или не маленький!");
            }
            return true;
        });
        
        // Команда для анимаций
        getCommand("anim").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length < 2) return false;
            try {
                int animId = Integer.parseInt(args[0]);
                Player small = Bukkit.getPlayer(args[1]);
                if (small != null) animationManager.playAnimation(big, small, animId);
            } catch (NumberFormatException e) {
                big.sendMessage("§cИспользуй: /anim <номер> <игрок>");
            }
            return true;
        });
        
        getCommand("carrypose").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player big)) return true;
            if (args.length < 2) return false;
            try {
                int poseId = Integer.parseInt(args[0]);
                Player small = Bukkit.getPlayer(args[1]);
                if (small != null) carryManager.setCarryPose(big, small, poseId);
            } catch (NumberFormatException e) {
                big.sendMessage("§cИспользуй: /carrypose <номер> <игрок>");
            }
            return true;
        });
        
        getCommand("sleeppose").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player small)) return true;
            if (args.length < 2) return false;
            try {
                int poseId = Integer.parseInt(args[0]);
                Player big = Bukkit.getPlayer(args[1]);
                if (big != null) sleepManager.startSleeping(small, big, poseId);
            } catch (NumberFormatException e) {
                small.sendMessage("§cИспользуй: /sleeppose <номер> <игрок>");
            }
            return true;
        });
        
        // ActionBar таск
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (carryManager.isCarrying(p)) {
                        p.sendActionBar("§e🔘 /poses §f- меню поз, §e/drop §f- опустить");
                    }
                    if (shoeManager.isInShoe(p)) {
                        p.sendActionBar("§7👟 Ты в кроссовке! /unsmoll чтобы вылезти");
                    }
                    if (sleepManager.isUnderBlanket(p)) {
                        p.sendActionBar("§b🛏️ Ты под одеялом! /stand чтобы встать");
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
        
        getLogger().info("§aTinyPlus Ultimate включён!");
    }
    
    public static TinyPlus getInstance() { return instance; }
    public TinyPlayer getTinyPlayer() { return tinyPlayer; }
    public PoseManager getPoseManager() { return poseManager; }
    public CarryManager getCarryManager() { return carryManager; }
    public SleepManager getSleepManager() { return sleepManager; }
    public AnimationManager getAnimationManager() { return animationManager; }
    public ShoeManager getShoeManager() { return shoeManager; }
    
    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player small)) return;
        Player big = e.getPlayer();
        
        if (!tinyPlayer.isSmall(small)) return;
        
        if (big.isSneaking()) {
            animationManager.openMenu(big, small);
        } else if (big.isSprinting()) {
            shoeManager.shoveIntoShoe(big, small);
        } else {
            carryManager.pickUp(big, small);
        }
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() == null) return;
        
        String itemName = e.getItem().getType().toString();
        String sitButton = getConfig().getString("buttons.sit");
        String lieButton = getConfig().getString("buttons.lie");
        String sitOnSmall = getConfig().getString("buttons.sit-on-small");
        String escapeShoe = getConfig().getString("buttons.escape-shoe");
        String escapeBlanket = getConfig().getString("buttons.escape-blanket");
        
        if (itemName.equalsIgnoreCase(sitButton)) {
            poseManager.toggleSit(p);
            e.setCancelled(true);
        } else if (itemName.equalsIgnoreCase(lieButton)) {
            poseManager.toggleLie(p);
            e.setCancelled(true);
        } else if (itemName.equalsIgnoreCase(sitOnSmall)) {
            Player target = p.getWorld().getPlayers().stream()
                .filter(pl -> tinyPlayer.isSmall(pl) && pl.getLocation().distance(p.getLocation()) < 2)
                .findFirst().orElse(null);
            if (target != null) poseManager.sitOnSmall(p, target);
            e.setCancelled(true);
        } else if (itemName.equalsIgnoreCase(escapeShoe)) {
            shoeManager.removeFromShoe(p);
            e.setCancelled(true);
        } else if (itemName.equalsIgnoreCase(escapeBlanket)) {
            sleepManager.escapeBlanket(p);
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        carryManager.onQuit(e.getPlayer());
        sleepManager.onQuit(e.getPlayer());
        shoeManager.onQuit(e.getPlayer());
        poseManager.onQuit(e.getPlayer());
    }
}
