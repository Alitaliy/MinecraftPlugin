package me.plugin.register.LoginData;

import java.util.ArrayList;
import java.util.List;

public final class LoginData {
    private static final List<String> Restricts = new ArrayList<>();
    //这里的 private 使得 Restricts 只能通过下面的三个 public 方法进行操作，
    // Restricts 不能被外部直接访问，受到保护。

    public static void addPlayerName(String playerNameIn){
        String convertedName = playerNameIn.toLowerCase();
        // toLowerCase 返回一个小写的副本，是 String 类的一个成员方法

        if (!Restricts.contains(convertedName)) {
            // contains 方法返回一个逻辑值，! 符号把它变为相反的值，
            // 因此这个 if 语句只有在 RESTRICTS 中不含 convertedName 时才会执行里面的部分
            Restricts.add(convertedName);
        }
    }

    public static void removePlayerName(String playerNameIn){
        String convertedName = playerNameIn.toLowerCase();

        Restricts.remove(convertedName);
    }

    public static boolean hasPlayerName(String playerNameIn){
        String convertedName = playerNameIn.toLowerCase();

        return Restricts.contains(convertedName);
    }
}