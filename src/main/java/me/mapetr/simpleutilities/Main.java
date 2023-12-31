package me.mapetr.simpleutilities;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import co.aikar.idb.*;
import me.mapetr.simpleutilities.commands.*;
import me.mapetr.simpleutilities.commands.teleport.Teleport;
import me.mapetr.simpleutilities.commands.teleport.TeleportHere;
import me.mapetr.simpleutilities.commands.warp.Warp;
import me.mapetr.simpleutilities.commands.warp.WarpSet;
import me.mapetr.simpleutilities.services.ChatService;
import me.mapetr.simpleutilities.services.PlayerListManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Main extends JavaPlugin implements Listener {
    FileConfiguration config = this.getConfig();
    PlayerListManager _playerListManager = new PlayerListManager(config);
    ChatService _chatService = new ChatService(config);
    @Override
    public void onEnable() {
        DatabaseOptions options = DatabaseOptions.builder().sqlite("plugins/simpleutilities/simpleutilities.db").build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS warps (name VARCHAR(255) PRIMARY KEY, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, world VARCHAR(255))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new Spectator());
        manager.registerCommand(new Kill());
        manager.registerCommand(new Teleport());
        manager.registerCommand(new TeleportHere());
        manager.registerCommand(new Warp());
        manager.registerCommand(new WarpSet());
        manager.enableUnstableAPI("help");

        CommandCompletions<BukkitCommandCompletionContext> completions = manager.getCommandCompletions();
        completions.registerAsyncCompletion("warps", c -> {
            try {
                return DB.getFirstColumnResults("SELECT name FROM warps");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new Main(), this);
        MiniMessage msg = MiniMessage.miniMessage();
        config.addDefault("footer", "I love foot <tps>");
        config.addDefault("header", "I love head <tps>");
        config.addDefault("delay", 100);
        config.addDefault("frequency", 200);
        config.addDefault("chat.format", "<color:#99AAB5><player>: <msg>");
        config.addDefault("chat.colors.name", "#7289DA");
        config.addDefault("chat.colors.msg", "#FFFFFF");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,() -> {
            _playerListManager.reloadGlobalPlayerList(msg);
        },config.getLong("delay"),config.getLong("frequency"));
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        DB.close();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        _playerListManager.reloadPlayerList(MiniMessage.miniMessage(), event.getPlayer());
    }
    @EventHandler
    public void onMessageSent(AsyncPlayerChatEvent event) {
        event.setCancelled(_chatService.processMessage(event, MiniMessage.miniMessage()));
    }
}
