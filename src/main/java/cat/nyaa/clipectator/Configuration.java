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
    public boolean autoRespawnToSpectator = false;

    @Serializable
    public boolean saveInventory = false;

    @Serializable
    public boolean allowBeyondBorder = true;

    @Serializable
    public boolean includeOp = false;

    @Serializable
    public boolean allowTeleport = true;

    @Serializable
    public boolean allowAllTransparent = false;

    @Serializable
    public List<UUID> ignoredPlayer = new ArrayList<>();

    @Serializable
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
