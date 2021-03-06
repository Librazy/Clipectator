package cat.nyaa.clipectator;

import cat.nyaa.nyaacore.Message;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.librazy.nyaautils_lang_checker.LangKey;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SpectatorListener implements Listener {
    private Main plugin;

    private Cache<UUID, Location> lastSafe = CacheBuilder.newBuilder()
                                                         .expireAfterWrite(10, TimeUnit.SECONDS)
                                                         .build();

    private Cache<UUID, String> rateLimit = CacheBuilder.newBuilder()
                                                        .expireAfterWrite(2, TimeUnit.SECONDS)
                                                        .build();

    private Cache<UUID, Long> alivePlayers = CacheBuilder.newBuilder()
                                                         .expireAfterWrite(60, TimeUnit.MINUTES) // emm好像这个expire没什么用
                                                         .build();

    SpectatorListener(Main pl) {
        pl.getServer().getPluginManager().registerEvents(this, pl);
        this.plugin = pl;
    }

    void RememberAlive() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.spectateOnLogin) {
                    this.cancel();
                }
                Bukkit.getServer().getOnlinePlayers().forEach(p -> {
                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        alivePlayers.put(p.getUniqueId(), System.currentTimeMillis());
                    }
                });
            }
        }.runTaskTimer(plugin, 0, 60 * 20);
    }

    private static Material getChestType(Block block) {
        Material material;
        if (block.getX() % 2 == 0) {
            material = Material.TRAPPED_CHEST;
        } else {
            material = Material.CHEST;
        }
        if (block.getY() % 2 == 0) {
            material = material.equals(Material.CHEST) ? Material.TRAPPED_CHEST : Material.CHEST;
        }
        if (block.getZ() % 2 == 0) {
            material = material.equals(Material.TRAPPED_CHEST) ? Material.CHEST : Material.TRAPPED_CHEST;
        }
        return material;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void OnPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
            alivePlayers.put(p.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnPlayerJoin(PlayerJoinEvent e) {
        if(!plugin.spectateOnLogin) return;
        Player p = e.getPlayer();
        if(plugin.config.enable
                   && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
                   && (!p.isOp() || plugin.config.includeOp)
                   && !plugin.config.ignoredPlayer.contains(p.getUniqueId())){
            Long last = alivePlayers.getIfPresent(p.getUniqueId());
            if(last == null){
                p.setGameMode(GameMode.SPECTATOR);
                msg(p, "user.offline.spec");
            } else {
                long delta = System.currentTimeMillis() - last;
                if(delta / 50 > plugin.config.maxOfflineTick){
                    new Message(I18n.format("user.offline.kill", p.getDisplayName())).broadcast(p.getWorld());
                    p.setHealth(0);
                } else {
                    alivePlayers.put(p.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnPlayerMove(PlayerMoveEvent e) throws ExecutionException {
        Player p = e.getPlayer();
        p.setSneaking(false);
        if (checkPlayer(p)) {
            Location to = e.getTo();
            if (isSafe(to)) {
                lastSafe.get(p.getUniqueId(), () -> to);
                return;
            }
            Location from = e.getFrom();
            Location delta = to.clone().subtract(from);
            Location mx = delta.clone();
            mx.setX(0);
            mx.add(from);
            e.setTo(mx);
            if (isSafe(mx)) {
                e.setTo(mx);
                return;
            }
            Location my = delta.clone();
            my.setY(0);
            my.add(from);
            if (isSafe(my)) {
                e.setTo(my);
                return;
            }
            Location mz = delta.clone();
            mz.setZ(0);
            mz.add(from);
            if (isSafe(mz)) {
                e.setTo(mz);
                return;
            }
            if (isSafe(from)) {
                msg(p, "user.move.fail");
                e.setCancelled(true);
                return;
            }
            Location safe = lastSafe.get(p.getUniqueId(), () -> p.getLocation().getWorld().getHighestBlockAt(p.getLocation()).getLocation().add(0, 0, 2));
            if (isSafe(safe)) {
                msg(p, "user.move.teleport_safe");
                p.teleport(safe);
            } else {
                Location spawn = p.getWorld().getSpawnLocation();
                p.teleport(spawn);
                msg(p, "user.move.teleport_worldspawn");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (checkPlayer(p)) {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && !plugin.config.allowTeleport) {
                e.setCancelled(true);
                msg(p, "user.teleport.fail");
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void OnPlayerDeath(PlayerDeathEvent e) {
        if (!(plugin.config.autoRespawnToSpectator || plugin.config.saveInventory) || !plugin.config.enable) return;
        Location l = e.getEntity().getLocation();
        Player p = e.getEntity();
        if (plugin.config.saveInventory
                    && e.getDrops() != null
                    && !e.getDrops().isEmpty()
                    && p.getLocation().getY() > 5) {
            for (int y = 0; y < 255; y++) {
                Location loc = p.getLocation().clone();
                if (loc.getY() + y >= loc.getWorld().getMaxHeight()) {
                    break;
                }
                loc.setY(loc.getY() + y);
                if (loc.getBlock().getType() == Material.AIR) {
                    Block block = loc.getBlock();
                    block.setType(getChestType(loc.getBlock()));
                    Chest chest = (Chest) block.getState();
                    try {
                        SimpleDateFormat format = new SimpleDateFormat(I18n.format("user.chest.date_format"));
                        String date = format.format(System.currentTimeMillis());
                        chest.setCustomName(I18n.format("user.chest.name", p.getName(), date));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    Inventory inventory = chest.getInventory();
                    HashMap<Integer, ItemStack> items = inventory.addItem(e.getDrops().toArray(new ItemStack[0]));
                    e.getDrops().clear();
                    e.getDrops().addAll(items.values());
                    if (e.getDrops().isEmpty()) {
                        e.setKeepInventory(false);
                        break;
                    }
                }
            }
        }

        if (plugin.config.autoRespawnToSpectator
                    && (!p.isOp() || plugin.config.includeOp)
                    && !plugin.config.ignoredPlayer.contains(p.getUniqueId())) {
            alivePlayers.invalidate(p.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.setGameMode(GameMode.SPECTATOR);
                Location loc = l.clone().add(0, 1, 0);
                if (isSafe(loc)) {
                    forceRespawn(p, loc);
                    return;
                }
                loc = lastSafe.getIfPresent(p.getUniqueId());
                if (loc != null && isSafe(loc)) {
                    forceRespawn(p, loc);
                    return;
                }
                loc = l.getWorld().getHighestBlockAt(p.getLocation()).getLocation().add(0, 0, 2);
                if (isSafe(loc)) {
                    forceRespawn(p, loc);
                    return;
                }
                loc = p.getWorld().getSpawnLocation();
                forceRespawn(p, loc);
            }, 1);
        }
    }

    private void forceRespawn(Player p, Location loc) {
        p.teleport(loc);
        p.spigot().respawn();
        p.teleport(loc);
    }

    private boolean checkPlayer(Player p) {
        return plugin.config.enable
                       && p.getGameMode() == GameMode.SPECTATOR
                       && (!p.isOp() || plugin.config.includeOp)
                       && !plugin.config.ignoredPlayer.contains(p.getUniqueId());
    }

    private boolean isSafe(Location l) {
        Location l1 = l.clone().add(0.3, 0.9, 0.3);
        Location l2 = l.clone().add(0.3, 0.9, -0.3);
        Location l3 = l.clone().add(-0.3, 0.9, 0.3);
        Location l4 = l.clone().add(-0.3, 0.9, -0.3);
        Location l5 = l.clone().add(0.3, 1.8, 0.3);
        Location l6 = l.clone().add(0.3, 1.8, -0.3);
        Location l7 = l.clone().add(-0.3, 1.8, 0.3);
        Location l8 = l.clone().add(-0.3, 1.8, -0.3);
        List<Location> bounding = Arrays.asList(l1, l2, l3, l4, l5, l6, l7, l8);
        return bounding.stream().unordered().parallel().map(Location::getBlock).distinct().map(Block::getType).allMatch(this::blockSafe)
                       && (plugin.config.allowBeyondBorder || !isOutsideBorder(l));
    }

    private boolean blockSafe(Material s) {
        return (s.isTransparent() && plugin.config.allowAllTransparent) || plugin.config.allowedBlock.contains(s.name());
    }

    private boolean isOutsideBorder(Location l) {
        WorldBorder border = l.getWorld().getWorldBorder();
        l = l.subtract(border.getCenter());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        double size = border.getSize() / 2;
        return x > size || z > size;
    }

    private void msg(Player target, @LangKey String template) {
        if (template.equals(rateLimit.getIfPresent(target.getUniqueId()))) {
            return;
        }
        rateLimit.put(target.getUniqueId(), template);
        target.sendMessage(I18n.format(template));
    }
}
