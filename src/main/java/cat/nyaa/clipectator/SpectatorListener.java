package cat.nyaa.clipectator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.librazy.nyaautils_lang_checker.LangKey;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SpectatorListener implements Listener {
    private Main plugin;

    private Cache<UUID, Location> lastSafe = CacheBuilder.newBuilder()
                                                         .expireAfterWrite(1, TimeUnit.MINUTES)
                                                         .build();

    private Cache<UUID, String> rateLimit = CacheBuilder.newBuilder()
                                                        .expireAfterWrite(2, TimeUnit.SECONDS)
                                                        .build();

    SpectatorListener(Main pl) {
        pl.getServer().getPluginManager().registerEvents(this, pl);
        this.plugin = pl;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnPlayerMove(PlayerMoveEvent e) throws ExecutionException {
        Player p = e.getPlayer();
        if (checkPlayer(p)) {
            Location from = e.getFrom();
            Location to = e.getTo();
            if (isSafe(to)) {
                lastSafe.put(p.getUniqueId(), to);
            } else {
                if (isSafe(from)) {
                    msg(p, "user.move.fail");
                    e.setCancelled(true);
                } else {
                    Location safe = lastSafe.get(p.getUniqueId(), ()-> p.getLocation().getWorld().getHighestBlockAt(p.getLocation()).getLocation().add(0, 0, 2));
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

    private boolean checkPlayer(Player p) {
        return plugin.config.enable
                       && p.getGameMode() == GameMode.SPECTATOR
                       && (!p.isOp() || plugin.config.includeOp)
                       && !plugin.config.ignoredPlayer.contains(p.getUniqueId())
                       && !p.hasPermission("clipectator.ignore");
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
        return bounding.stream().unordered().map(Location::getBlock).distinct().map(Block::getType).allMatch(this::blockSafe)
                       && (plugin.config.allowBeyondBorder || isOutsideBorder(l));
    }

    private boolean blockSafe(Material s) {
        return (s.isTransparent() && plugin.config.allowAllTransparent) || plugin.config.allowedBlock.contains(s.name());
    }

    private boolean isOutsideBorder(Location l) {
        WorldBorder border = l.getWorld().getWorldBorder();
        double radius = border.getSize() / 2;
        Location center = border.getCenter();
        return center.distanceSquared(l) >= (radius * radius);
    }

    private void msg(Player target, @LangKey String template) {
        if (template.equals(rateLimit.getIfPresent(target.getUniqueId()))) {
            return;
        }
        rateLimit.put(target.getUniqueId(), template);
        target.sendMessage(I18n.format(template));
    }
}
