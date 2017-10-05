package cat.nyaa.clipectator;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Configuration extends PluginConfigure {
    private final Main plugin;

    @Serializable
    public String language = "en_US";

    @Serializable
    public boolean enable = true;

    @Serializable
    //自动自动变成观察者
    public boolean autoRespawnToSpectator = false;

    @Serializable
    //自动成盒
    public boolean saveInventory = false;

    @Serializable
    //允许受限玩家出圈
    public boolean allowBeyondBorder = true;

    @Serializable
    //OP也会受限
    public boolean includeOp = false;

    @Serializable
    //允许使用观察者的TP
    public boolean allowTeleport = true;

    @Serializable
    //允许穿过所有透明方块
    public boolean allowAllTransparent = false;

    @Serializable
    //忽略玩家
    public List<UUID> ignoredPlayer = new ArrayList<>();

    @Serializable
    //允许穿过的方块
    public List<String> allowedBlock = Stream.of(Material.AIR, Material.WATER, Material.LAVA, Material.STATIONARY_WATER, Material.STATIONARY_LAVA).map(Material::name).collect(Collectors.toList());

    public Configuration(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }
}
