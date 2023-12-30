package me.plugin.registersmart.registersmart;

import java.util.*;

public class RuntimeDataManager {
    private static final List <UUID> RESTRICTS = new ArrayList<>();
    private static final Map <UUID,Integer> IFORGOT_SETUP_MAP = new HashMap<>();
    private static final List <UUID> READ_MODE_LIST = new ArrayList<>();

    public synchronized static void addRestrictUUID(UUID id){
        RESTRICTS.add(id);
    }

    public synchronized static void removeRestrictUUID(UUID id){
        RESTRICTS.remove(id);
    }

    public synchronized static boolean hasRestrictUUID(UUID id){
        return RESTRICTS.contains(id);
    }

    public synchronized static void toReadMode(UUID id){
        READ_MODE_LIST.add(id);
    }

    public synchronized static void exitReadMode(UUID id){
        READ_MODE_LIST.remove(id);
    }

    public synchronized  static boolean isInReadMode(UUID id){
        return READ_MODE_LIST.contains(id);
    }

    public synchronized static void toIForgotMode(UUID id, int mode){
        IFORGOT_SETUP_MAP.put(id,mode);
    }

    public synchronized static void exitIForgotMode(UUID id){
        IFORGOT_SETUP_MAP.remove(id);
    }

    public synchronized static int getIForgotMode(UUID id){
        return Objects.requireNonNullElse(IFORGOT_SETUP_MAP.get(id),0);
    }

    //READ_MODE_LIST 记录哪些 OP 正在审核玩家的密码恢复请求。
    // IFORGOT_SETUP_MAP 记录哪些玩家正在请求密码恢复以及恢复到哪一步了。
    //
    //审核模式下，OP 可以审核玩家的请求，此时禁用命令。
    //
    //「IForgot」模式有两步，输入新密码和输入理由，此时禁用命令。
    //
    //基于此创建了上面这些代码，应该非常简单。
    //
    //这次我们基于 UUID 来管理玩家。
    //
    //唯一出现的新知识点就是 synchronized，它的意思是「同步」，也就是说，阻止多个线程同时访问一个对象，这很明显。
    // ArrayList 不是线程安全（Thread Safe）的，因此要阻止它们同时被多个线程写入，稍微牺牲了一点性能但增加了安全性。
    // 至于 IDataManager，它的实例属于各个线程，因此不影响。
}
