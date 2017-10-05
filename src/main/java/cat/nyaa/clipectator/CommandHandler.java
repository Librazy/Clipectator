package cat.nyaa.clipectator;


import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class CommandHandler extends CommandReceiver {
    private final Main plugin;

    public CommandHandler(Main plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "reload", permission = "clipectator.admin")
    public void commandReload(CommandSender sender, Arguments args) {
        plugin.reload();
    }

    @SubCommand(value = "disable", permission = "clipectator.admin")
    public void commandDisable(CommandSender sender, Arguments args) {
        plugin.config.enable = false;
        plugin.config.save();
    }

    @SubCommand(value = "enable", permission = "clipectator.admin")
    public void commandEnable(CommandSender sender, Arguments args) {
        plugin.config.enable = true;
        plugin.config.save();
    }

    @SubCommand(value = "allow", permission = "clipectator.admin")
    public void commandAllow(CommandSender sender, Arguments args) {
        Material m = args.nextEnum(Material.class);
        plugin.config.allowedBlock.add(m.name());
        plugin.config.save();
    }

    @SubCommand(value = "disallow", permission = "clipectator.admin")
    public void commandDisallow(CommandSender sender, Arguments args) {
        Material m = args.nextEnum(Material.class);
        plugin.config.allowedBlock.remove(m.name());
        plugin.config.save();
    }
}
