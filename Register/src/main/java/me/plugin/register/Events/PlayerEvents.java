package me.plugin.register.Events;

import me.plugin.register.LoginData.LoginData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class PlayerEvents implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent ple){
        LoginData.addPlayerName(ple.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent pqe){
        LoginData.removePlayerName(pqe.getPlayer().getName());
    }

    public static void cancelIfNotLoggedIn(Cancellable e){
        if(e instanceof PlayerEvent){
            // instanceof 关键字指示 Java 重新判断左边对象的类型是不是右边的类或者右边类的子类，
            // 也就是判断能否进行强制类型转换
            if (LoginData.hasPlayerName(((PlayerEvent) e).getPlayer().getName())){
                // if 语句用于看看玩家是不是在限制列表中
                // (PlayerEvent) e 进行类型转换
                e.setCancelled(true);
            }
        }
        else if(e instanceof InventoryOpenEvent){
            // 限制玩家打开物品栏，需要 InventoryOpenEvent
            if(LoginData.hasPlayerName(((InventoryOpenEvent) e).getPlayer().getName())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void restrictMove(PlayerMoveEvent pme){
        cancelIfNotLoggedIn(pme);
    }//阻止未注册玩家移动

    @EventHandler
    public void restrictInteract(PlayerInteractEvent pie){
        cancelIfNotLoggedIn(pie);
    }//阻止未注册玩家交互

    @EventHandler
    public void restrictInteractAtEntity(PlayerInteractAtEntityEvent piaee){
        cancelIfNotLoggedIn(piaee);
    }//阻止未注册玩家实体交互

    @EventHandler
    public void restrictPortal(PlayerPortalEvent ppe){
        cancelIfNotLoggedIn(ppe);
    }//阻止未注册玩家使用传送门

    @EventHandler
    public void restrictTeleport(PlayerTeleportEvent pte){
        cancelIfNotLoggedIn(pte);
    }//阻止未注册玩家传送

    @EventHandler
    public void restrictOpenInventory(InventoryOpenEvent ioe){
        cancelIfNotLoggedIn(ioe);
    }//阻止未注册玩家打开物品栏
}
