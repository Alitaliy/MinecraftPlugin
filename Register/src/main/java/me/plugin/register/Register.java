package me.plugin.register;

import me.plugin.register.Commands.CommandHandler;
import me.plugin.register.Events.PlayerEvents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public class Register extends JavaPlugin {
    public static JavaPlugin instance;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        // 如果配置文件不存在，Bukkit 会保存默认的配置
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(), this);
        // 注册事件处理器，这里必须实例化，this 表明注册到本插件上
        Objects.requireNonNull(Bukkit.getPluginCommand("login")).setExecutor(new CommandHandler());
        // 注册事件处理器，也要实例化，requireNonNull 是不必要的，但是万一插件损坏了或者 Bukkit 出错了，我们还能知道是这里出问题


        instance = this;
        // 小技巧：暴露实例
    }

    @Override
    public void onDisable() {
        saveConfig();
        // 保存配置
    }
}
