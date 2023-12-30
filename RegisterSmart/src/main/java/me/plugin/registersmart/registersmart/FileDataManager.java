package me.plugin.registersmart.registersmart;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class FileDataManager implements IDataManager {
    static FileConfiguration data;
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // Date 没有办法直接序列化，需要利用 DateFormat

    @Override
    public void saveAll() {
        try {
            File dataFile = new File(RegisterSmart.instance.getDataFolder(), "data.yml");
            data.save(dataFile);
            // 保存数据的标准方法
        } catch (IOException e) {
            RegisterSmart.instance.getLogger().log(Level.WARNING, "配置数据未能保存，可能产生回档问题！");
            // 这种错误还是说出来的好
            e.printStackTrace();
        }
    }

    @Override
    public void loadAll() {
        File dataFile = new File(RegisterSmart.instance.getDataFolder(), "data.yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
        // 不需要 InputStream，直接 loadConfiguration
    }

    @Override
    @Nonnull
    public String getPasswordHash(UUID id) {
        return Objects.requireNonNull(data.getString("passwords." + id.toString(), ""));
        // 虽然 getString 提供了默认值就不会返回 null，但 IDEA 一直报警告很麻烦，就照它的建议做了
    }

    @Override
    public boolean getIForgotState(UUID id) {
        return data.getBoolean("iforgot-states." + id.toString());
        // boolean 不会返回 null，默认是 false
    }

    @Override
    @Nonnull
    public String getIForgotManualReason(UUID id) {
        return Objects.requireNonNull(data.getString("iforgot-reasons." + id.toString(), ""));
    }

    @Override
    @Nonnull
    public String getIForgotNewPasswordHash(UUID id) {
        // IForgot 会先向玩家要求一个新密码，用这个查询
        return Objects.requireNonNull(data.getString("iforgot-newpwd." + id.toString(), ""));
    }

    @Override
    @Nonnull
    public Date getLastLoginTime(UUID id) {
        String dstr = data.getString("last-login." + id.toString(), "1970-01-01 23:59:59");
        if (dstr == null) {
            // 实际上这里不可能执行到，getString 返回的不可能是 null
            try {
                return sdf.parse("1970-01-01 23:59:59");
            } catch (ParseException e) {
                RegisterSmart.instance.getLogger().log(Level.WARNING, "这不可能！不可能出现这个错误！日期的读取失败了？");
                e.printStackTrace();
                return new Date();
            }
        } else {
            try {
                return sdf.parse(dstr);
            } catch (ParseException e) {
                // 这里也不可能执行到，以防万一
                try {
                    return sdf.parse("1970-01-01 23:59:59");
                } catch (ParseException e2) {
                    RegisterSmart.instance.getLogger().log(Level.WARNING, "这不可能！不可能出现这个错误！日期的读取失败了？");
                    e2.printStackTrace();
                    return new Date();
                }
            }
        }
    }

    @Override
    public UUID getNextRequest(){
        Set<String> keys = Objects.requireNonNull(data.getConfigurationSection("iforgot-states")).getKeys(false);
        for(String s : keys){
            if(data.getBoolean("iforgot-states." + s)){
                return UUID.fromString(s);
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    // 以下都是上面相应的 set 方法
    @Override
    public void setPasswordHash(UUID id, String hash) {
        data.set("passwords." + id.toString(), hash);
    }

    @Override
    public void setIForgotState(UUID id, boolean state) {
        data.set("iforgot-states." + id.toString(), state);
    }

    @Override
    public void setIForgotManualReason(UUID id, String reason) {
        data.set("iforgot-reasons." + id.toString(), reason);
    }

    @Override
    public void setIForgotNewPasswordHash(UUID id, String hash) {
        data.set("iforgot-newpwd." + id.toString(), hash);
    }

    @Override
    public void setLastLoginTime(UUID id, Date date) {
        data.set("last-login." + id.toString(), sdf.format(date));
    }

    @Override
    public boolean isExist(UUID id) {
        return data.contains("passwords." + id.toString());
    }
}
