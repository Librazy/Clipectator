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
        try{
            Material m = args.nextEnum(Material.class);
            plugin.config.allowedBlock.add(m.name());
            plugin.config.save();
            msg(sender, "admin.allowedblock.added", m.name());
        }catch (BadCommandException ignore){}
        msg(sender, "admin.allowedblock.list", plugin.config.allowedBlock.stream().reduce((a, b) -> a + ", " + b).orElse(""), plugin.config.allowAllTransparent);
    }

    @SubCommand(value = "disallow", permission = "clipectator.admin")
    public void commandDisallow(CommandSender sender, Arguments args) {
        try{
            Material m = args.nextEnum(Material.class);
            plugin.config.allowedBlock.remove(m.name());
            plugin.config.save();
            msg(sender, "admin.allowedblock.removed", m.name());
        }catch (BadCommandException ignore){}
        msg(sender, "admin.allowedblock.list", plugin.config.allowedBlock.stream().reduce((a, b) -> a + ", " + b).orElse(""), plugin.config.allowAllTransparent);

    }
    @SubCommand(value = "allowteleport", permission = "clipectator.admin")
    public void commandAllowTeleport(CommandSender sender, Arguments args) {
        try{
            plugin.config.allowTeleport = args.nextBoolean();
            msg(sender, "admin.allowteleport.set", plugin.config.allowTeleport);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.allowteleport.state", plugin.config.allowTeleport);
        }
    }
    @SubCommand(value = "allowbeyondborder", permission = "clipectator.admin")
    public void commandAllowBeyondBorder(CommandSender sender, Arguments args) {
        try{
            plugin.config.allowBeyondBorder = args.nextBoolean();
            msg(sender, "admin.allowbeyondborder.set", plugin.config.allowBeyondBorder);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.allowbeyondborder.state", plugin.config.allowBeyondBorder);
        }
    }

    @SubCommand(value = "allowalltransparent", permission = "clipectator.admin")
    public void commandAllowAllTransparent(CommandSender sender, Arguments args) {
        try{
            plugin.config.allowAllTransparent = args.nextBoolean();
            msg(sender, "admin.allowalltransparent.set", plugin.config.allowAllTransparent);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.allowalltransparent.state", plugin.config.allowAllTransparent);
        }
    }

    @SubCommand(value = "autorespawn", permission = "clipectator.admin")
    public void commandAutoRespawn(CommandSender sender, Arguments args) {
        try{
            plugin.config.autoRespawnToSpectator = args.nextBoolean();
            msg(sender, "admin.autorespawn.set", plugin.config.autoRespawnToSpectator);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.autorespawn.state", plugin.config.autoRespawnToSpectator);
        }
    }

    @SubCommand(value = "includeop", permission = "clipectator.admin")
    public void commandIncludeOp(CommandSender sender, Arguments args) {
        try{
            plugin.config.includeOp = args.nextBoolean();
            msg(sender, "admin.includeop.set", plugin.config.includeOp);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.includeop.state", plugin.config.includeOp);
        }
    }

    @SubCommand(value = "saveinventory", permission = "clipectator.admin")
    public void commandSaveInventory(CommandSender sender, Arguments args) {
        try{
            plugin.config.saveInventory = args.nextBoolean();
            msg(sender, "admin.saveinventory.set", plugin.config.saveInventory);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.saveinventory.state", plugin.config.saveInventory);
        }
    }

    @SubCommand(value = "maxofflinetick", permission = "clipectator.admin")
    public void commandMaxOfflineTick(CommandSender sender, Arguments args) {
        try{
            plugin.config.maxOfflineTick = args.nextInt();
            msg(sender, "admin.maxofflinetick.set", plugin.config.maxOfflineTick);
            plugin.config.save();
        }catch (BadCommandException ignore){
            msg(sender, "admin.maxofflinetick.state", plugin.config.maxOfflineTick);
        }
    }

    @SubCommand(value = "spectateonlogin", permission = "clipectator.admin")
    public void commandSpectateOnLogin(CommandSender sender, Arguments args) {
        try{
            plugin.spectateOnLogin = args.nextBoolean();
            msg(sender, "admin.spectateonlogin.set", plugin.spectateOnLogin);
            if(plugin.spectateOnLogin){
                plugin.specListener.RememberAlive();
            }
        }catch (BadCommandException ignore){
            msg(sender, "admin.spectateonlogin.state", plugin.spectateOnLogin);
        }
    }

    @SubCommand(value = "version")
    public void version(CommandSender sender, Arguments args) {
        String ver = plugin.getDescription().getVersion();
        msg(sender, "manual.license", ver, plugin.getDescription().getAuthors().stream().reduce((a, b) -> a + ", " +b).orElse("NyaaCat Developers"));
    }
}
