package net.simplyvanilla.simplychat;

import net.simplyvanilla.simplychat.command.MessageCommandExecutor;
import net.simplyvanilla.simplychat.command.ReplyCommandExecutor;
import net.simplyvanilla.simplychat.state.PlayerStateManager;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SimplyChatPlugin extends JavaPlugin {

    private static SimplyChatPlugin instance;

    private PlayerStateManager playerStateManager;
    private String format;

    @Override
    public void onLoad() {
        SimplyChatPlugin.instance = this;
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        if (pm.getPlugin("PlaceholderAPI") == null) {
            getLogger().log(Level.SEVERE, "Could not find PlaceholderAPI! This plugin is required.");
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

        getCommand("msg").setExecutor(messageCommandExecutor);
        getCommand("reply").setExecutor(new ReplyCommandExecutor(messageCommandExecutor));

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

    public String getColorCodeTranslatedConfigString(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }

    public String getFormat() {
        return this.format;
    }

    public static SimplyChatPlugin getInstance() {
        return SimplyChatPlugin.instance;
    }

}
