package cat.nyaa.clipectator;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public Configuration config;
    public I18n i18n;
    public CommandHandler commandHandler;
    public SpectatorListener specListener;

    @Override
    public void onEnable() {
        config = new Configuration(this);
        config.load();
        i18n = new I18n(this, this.config.language);
        i18n.load();
        commandHandler = new CommandHandler(this, this.i18n);
        getCommand("clipectator").setExecutor(commandHandler);
        getCommand("clipectator").setTabCompleter(commandHandler);
        specListener = new SpectatorListener(this);
    }

    @Override
    public void onDisable() {
        disable(true);
    }

    public void disable(boolean saveConfig) {
        getServer().getScheduler().cancelTasks(this);
        getCommand("clipectator").setExecutor(null);
        getCommand("clipectator").setTabCompleter(null);
        HandlerList.unregisterAll(this);
        if (saveConfig) {
            config.save();
        }
    }

    public void reload() {
        disable(false);
        onEnable();
    }
}
