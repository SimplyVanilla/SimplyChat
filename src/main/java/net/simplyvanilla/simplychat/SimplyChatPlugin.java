package net.simplyvanilla.simplychat;

import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.simplyvanilla.simplychat.command.IgnoreCommandExecutor;
import net.simplyvanilla.simplychat.command.MessageCommandExecutor;
import net.simplyvanilla.simplychat.command.ReplyCommandExecutor;
import net.simplyvanilla.simplychat.database.Cache;
import net.simplyvanilla.simplychat.database.MYSQL;
import net.simplyvanilla.simplychat.state.PlayerStateManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplyChatPlugin extends JavaPlugin {

    private static SimplyChatPlugin instance;

    private PlayerStateManager playerStateManager;
    private String format;

    private MYSQL database;
    private Cache cache;

    @Override
    public void onLoad() {
        SimplyChatPlugin.instance = this;
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        if (pm.getPlugin("MiniPlaceholders") == null) {
            getLogger().log(Level.SEVERE,
                "Could not find MiniPlaceholders! This plugin is required.");
            pm.disablePlugin(this);
            return;
        }

        try {
            saveDefaultConfig();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load config file! Disabling plugin...", e);
            pm.disablePlugin(this);
            return;
        }

        this.playerStateManager = new PlayerStateManager();
        this.format = getConfig().getString("chat.format");

        MessageCommandExecutor messageCommandExecutor = new MessageCommandExecutor();

        database = new MYSQL();
        database.connect();
        cache = new Cache(database);

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                cache.loadPlayerIgnoreList(onlinePlayer);
            }
        }

        getCommand("msg").setExecutor(messageCommandExecutor);
        getCommand("reply").setExecutor(new ReplyCommandExecutor(messageCommandExecutor));
        getCommand("ignore").setExecutor(new IgnoreCommandExecutor());
        getCommand("unignore").setExecutor(new IgnoreCommandExecutor());
        getCommand("ignorelist").setExecutor(new IgnoreCommandExecutor());

        pm.registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        SimplyChatPlugin.instance = null;
    }

    public PlayerStateManager getPlayerStateManager() {
        return this.playerStateManager;
    }

    public Component getColorCodeTranslatedConfigString(String path, TagResolver... resolvers) {
        return MiniMessage.miniMessage()
            .deserialize(Objects.requireNonNull(getConfig().getString(path)), resolvers);
    }

    public String getConfigString(String path) {
        return getConfig().getString(path);
    }

    public String getFormat() {
        return this.format;
    }

    public static SimplyChatPlugin getInstance() {
        return SimplyChatPlugin.instance;
    }

    public MYSQL getDatabase() {
        return database;
    }

    public Cache getCache() {
        return cache;
    }
}
