package net.simplyvanilla;

import net.simplyvanilla.command.MessageCommandExecutor;
import net.simplyvanilla.command.ReplyCommandExecutor;
import net.simplyvanilla.listener.AsyncPlayerChatEventListener;
import net.simplyvanilla.listener.PlayerQuitEventListener;
import net.simplyvanilla.state.PlayerStateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

public class SimplyChatPlugin extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PluginManager pm = getServer().getPluginManager();

            try {
                config = loadConfig();
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().log(Level.SEVERE, "Could not load config file! Disabling plugin...");
                pm.disablePlugin(this);
                return;
            }

            PlayerStateManager playerStateManager = new PlayerStateManager();

            MessageCommandExecutor messageCommandExecutor = new MessageCommandExecutor(
                    getColorCodeTranslatedConfigString("command.message.helpMessage"),
                    getColorCodeTranslatedConfigString("command.message.receiverNotFoundMessage"),
                    getColorCodeTranslatedConfigString("command.message.senderMessageFormat"),
                    getColorCodeTranslatedConfigString("command.message.receiverMessageFormat"),
                    playerStateManager
            );

            ReplyCommandExecutor replyCommandExecutor = new ReplyCommandExecutor(
                    getColorCodeTranslatedConfigString("command.reply.helpMessage"),
                    getColorCodeTranslatedConfigString("command.reply.noReceiverMessage"),
                    getColorCodeTranslatedConfigString("command.reply.receiverNotOnlineMessage"),
                    messageCommandExecutor,
                    playerStateManager
            );

            getCommand("message").setExecutor(messageCommandExecutor);
            getCommand("reply").setExecutor(replyCommandExecutor);

            pm.registerEvents(new AsyncPlayerChatEventListener(config.getString("chat.format")), this);
            pm.registerEvents(new PlayerQuitEventListener(playerStateManager), this);
        } else {
            getLogger().log(Level.WARNING, "Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private String getColorCodeTranslatedConfigString(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }

    private FileConfiguration loadConfig() throws IOException {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            Files.copy(getClassLoader().getResourceAsStream("config.yml"), configFile.toPath());
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

}
