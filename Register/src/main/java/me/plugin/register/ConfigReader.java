package me.plugin.register;


import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigReader {

    public static FileConfiguration config = Register.instance.getConfig();
    //

    public static boolean isPlayerRegistered(String playerName){
        return config.contains(playerName.toLowerCase());
    }//是否注册

    public static  boolean verifyPassword(String playerName,String password){
        return  password.equals(config.getString(playerName.toLowerCase()));
        // 三步合成一行：转换小写，读取字符串，返回是否相等
    }//验证密码

    public static void addPlayer(String playerName,String password){
        Register.instance.getConfig().set(playerName.toLowerCase(),password);
        //这里的实现也很简单，我们通过 getConfig 获得了插件的配置文件，
        // 然后通过 getString 读取，set 设置，
        // 分别实现了「是否存在」、「密码是否正确」和「添加玩家」的功能。
        // 同样，这里把名字转换为了小写。
        Register.instance.saveConfig();
    }//注册
}
