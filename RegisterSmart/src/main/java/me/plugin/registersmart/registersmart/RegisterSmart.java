package me.plugin.registersmart.registersmart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public final class RegisterSmart extends JavaPlugin {

    public static  JavaPlugin instance;
    public static boolean dbError = false;

    @Override
    public void onEnable() {
        // Plugin startup logic

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.WARNING,"数据库驱动加载失败，将使用备用存储方法");
            e.printStackTrace();
            dbError = true;
            //如果驱动找不到就该用文件存储
        }
        saveDefaultConfig();//config.yml
        saveResource("data.yml",false);//data.yml,false表示不覆盖
        //saveResource 用于保存 src 下对应的文件，第二个参数是「文件存在时，是否覆盖」的意思。
        instance = this;
        new DBDataManager().loadAll();
        new FileDataManager().loadAll();

        Bukkit.getPluginManager().registerEvents(new EventRegister(),this);
        Objects.requireNonNull(Bukkit.getPluginCommand("hl")).setExecutor(new Commands());
        Objects.requireNonNull(Bukkit.getPluginCommand("iforgot")).setExecutor(new Commands());

        if(getConfig().getBoolean("mysql.enabled")){
            new DBDataManager().loadAll();
        }

        getLogger().info(ChatColor.YELLOW + "[RegisterSmart]" + ChatColor.GREEN + "已加载");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        new DBDataManager().saveAll();
        new FileDataManager().saveAll();
        getLogger().info(ChatColor.YELLOW + "[RegisterSmart]" + ChatColor.RED + "已卸载");
    }
}
