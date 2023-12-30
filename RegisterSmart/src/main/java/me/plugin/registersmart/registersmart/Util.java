package me.plugin.registersmart.registersmart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class Util {

    @Nonnull
    public static String getAndTranslate(@Nonnull String key){
        String str = Objects.requireNonNullElse(RegisterSmart.instance.getConfig().getString(key,""),"");
        return ChatColor.translateAlternateColorCodes('&',str);
        //用于替换 § 为 &，方便配置
    }

    @Nonnull
    public static String calculateMD5(@Nonnull String origin){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(origin.getBytes(StandardCharsets.UTF_8));
            //加码
            return String.valueOf(Hex.encodeHex(md.digest()));
            //转换为十六进制
        }catch (NoSuchAlgorithmException e){
            RegisterSmart.instance.getLogger().severe("必要的MD5哈希算法不可用，正在禁用本插件");
            //线代 CentOS 和 Windows Server都有这个算法，应该不成问题
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(RegisterSmart.instance);
            //紧急停止插件，此时任何值都不明智
            //disablePlugin 用于停止插件，当出现不可恢复错误时才使用。
            return "";
        }
    }

    //用于生成命令钩子
    public static List<String> generateHooks(@Nonnull String key, @Nonnull String playerName){
        List<String> origin = RegisterSmart.instance.getConfig().getStringList(key);
        List<String> output = new ArrayList<>();
        for(String cmd : origin) {
            if (cmd != null & !cmd.equals("")) {
                output.add(cmd.replaceAll("\\$\\{playerName}", playerName));
                //模板替换
            }
        }
        return output;
    }

    public static synchronized void dispatchCommandAsServer(String cmd){
        new BukkitRunnable(){
            @Override
            public void run(){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }
        }.runTask(RegisterSmart.instance);
    }//执行命令

}
