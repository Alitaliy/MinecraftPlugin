package me.plugin.registersmart.registersmart;

import org.apache.logging.log4j.util.PropertySource;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Commands implements CommandExecutor {

    public static List<UUID> NoInterruptList = new ArrayList<>();
    //那些玩家的命令正在执行中

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender,Command command, String label,String[] args) {

        if(!(sender instanceof Player)){
            //组织控制台登录
            return false;
        }
        UUID id = ((Player) sender).getUniqueId();
        //强制转换并获取UUID

        if(RuntimeDataManager.getIForgotMode(id) != 0 || RuntimeDataManager.isInReadMode(id)){
            //在审核模式或者恢复模式中，禁止命令
            return true;
        }

        if(getIF(id)){
            sender.sendMessage((Util.getAndTranslate("msg.command-handling")));
            //当前尚有命令没有处理完成，拒绝处理
            return true;
        }
        cli(id);//暂时禁止该玩家命令
        if(command.getName().equals("hl")){
            return onLoginCommand(sender,args);
            //分配到 onLoginCommand 中
        }
        else if(command.getName().equals("iforgot")){
            if(!RegisterSmart.instance.getConfig().getBoolean("iforgot")){
                sender.sendMessage(Util.getAndTranslate("msg.iforgot-no-available"));
                //如果恢复模式被禁用
                sti(id);//允许该玩家继续执行命令
                return true;
            }
            return onIForgotCommand(sender);
            //切换到 onIForgotCommand 继续执行
        }
        else{
            //后备操作
            sti(id);
            //卡放命令
            return false;
        }

    }

    public boolean onLoginCommand(CommandSender sender,String[] args){
        Player player = (Player) sender;
        UUID id = player.getUniqueId();
        if(RuntimeDataManager.hasRestrictUUID(id)){
            //进入异步处理
            new BukkitRunnable(){
                @Override
                public void run(){
                    IDataManager idm;//仅【占个位置】 ，下面按需赋值
                    if(RegisterSmart.instance.getConfig().getBoolean("mysql.enabled") && !RegisterSmart.dbError){
                        //数据库可用
                        idm = new DBDataManager();
                    }
                    else{
                        //数据库不可用
                        idm = new FileDataManager();
                    }
                    if(idm.isExist(id)){
                        //存在ID
                        //很方便
                        if(args[0] == null){
                            //没输入密码
                            player.sendMessage(Util.getAndTranslate("msg.login-failed"));
                            sti(id);//开放命令
                            List<String> hooks = Util.generateHooks("hooks.on-login-failed",player.getName());
                            for(String cmd : hooks){
                                //按顺序循环hooks中的每项
                                Util.dispatchCommandAsServer(cmd);
                                //以服务器身份执行命令
                            }
                            //执行钩子
                            return;
                        }
                        if(idm.getPasswordHash(id).equals(Util.calculateMD5(args[0]))){
                            RuntimeDataManager.removeRestrictUUID(id);
                            player.setWalkSpeed(EventRegister.originSpeed.get(id));
                            player.sendMessage(Util.getAndTranslate("msg.login-success"));
                            idm.setIForgotManualReason(id,"");
                            idm.setIForgotState(id,false);
                            idm.setLastLoginTime(id,new Date());
                            //登陆成功，重设日期，取消恢复请求
                            sti(id);
                            List<String> hooks = Util.generateHooks("hooks.on-login-success",player.getName());
                            for(String cmd : hooks){
                                Util.dispatchCommandAsServer(cmd);
                            }
                            return;
                        }
                        player.sendMessage(Util.getAndTranslate("msg.login-failed"));
                        List<String>hooks = Util.generateHooks("hooks.on-login-failed",player.getName());
                        for(String cmd : hooks){
                            Util.dispatchCommandAsServer(cmd);
                        }
                        sti(id);
                        return;
                    }
                    if(args.length < 2 || !args[0].equals(args[1])){
                        player.sendMessage(Util.getAndTranslate("msg.register-failed"));
                        sti(id);
                        return;
                    }
                    idm.setPasswordHash(id,Util.calculateMD5(args[0]));
                    RuntimeDataManager.removeRestrictUUID(id);
                    player.setWalkSpeed(EventRegister.originSpeed.get(id));
                    player.sendMessage(Util.getAndTranslate("msg.register-success"));
                    idm.setIForgotManualReason(id,"");
                    idm.setIForgotState(id,false);
                    idm.setLastLoginTime(id,new Date());
                    sti(id);
                    List<String> hooks = Util.generateHooks("hooks.on-register-success",player.getName());
                    for(String cmd : hooks){
                        Util.dispatchCommandAsServer(cmd);
                    }
                }
            }.runTaskAsynchronously(RegisterSmart.instance);
        }
        else{
            player.sendMessage(Util.getAndTranslate("msg.login-success"));
            //已经登录
            sti(id);
        }
        return true;

    }

    public boolean onIForgotCommand (CommandSender sender){
        Player player = (Player) sender;
        UUID id = player.getUniqueId();

        new BukkitRunnable(){
            @Override
            public void run(){
                IDataManager idm;
                if(RegisterSmart.instance.getConfig().getBoolean("mysql.enabled") && !RegisterSmart.dbError){
                    idm = new DBDataManager();
                }
                else{
                    idm = new FileDataManager();
                }
                if(!RuntimeDataManager.hasRestrictUUID(id)){
                    if(!player.isOp()){
                        idm.setIForgotState(id,false);
                        idm.setIForgotManualReason(id,"");
                        RuntimeDataManager.toIForgotMode(id,1);


                        player.sendMessage(Util.getAndTranslate("msg.iforgot-newpwd"));
                    }
                    else{
                        RuntimeDataManager.toReadMode(id);
                        player.sendMessage(Util.getAndTranslate("msg.audit-in"));
                        UUID firstId = idm.getNextRequest();
                        //先获取一个请求，开始这个链式反应
                        EventRegister.lastJudgeUUID.put(player.getUniqueId(),firstId);
                        //判断op上一次操作的是哪个UUID
                        if(firstId.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))){
                            RuntimeDataManager.exitReadMode(id);
                            player.sendMessage(Util.getAndTranslate("ms.audit-out"));
                        }
                        else{
                            player.sendMessage(Util.getAndTranslate("msg.audit-uuid") + firstId.toString());
                            player.sendMessage(Util.getAndTranslate("msg.audit-reason") + idm.getIForgotManualReason(firstId));
                            player.sendMessage(Util.getAndTranslate("msg.audit-hint"));
                        }
                    }
                }
                else{
                    RuntimeDataManager.toIForgotMode(id,1);
                    player.sendMessage(Util.getAndTranslate("msg.iforgot-newpwd"));
                }
                sti(id);
            }
        }.runTaskAsynchronously(RegisterSmart.instance);
        return true;
    }

    private static synchronized void cli(UUID id){
        NoInterruptList.add(id);
    }

    private static synchronized void sti(UUID id){
        NoInterruptList.remove(id);
    }
    //cli 和 sti 分别禁止命令执行和允许命令执行，
    // 我这么命名只是因为它们的功能和汇编中的
    // CLI（Clear Interrupt，禁止中断）和 STI（Set Interrupt，允许中断）指令很像啦。

    private static synchronized boolean getIF(UUID id){
        return NoInterruptList.contains(id);
    }
}
