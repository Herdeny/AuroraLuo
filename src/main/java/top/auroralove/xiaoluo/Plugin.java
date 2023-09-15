package top.auroralove.xiaoluo;

import xyz.cssxsh.mirai.tool.FixProtocolVersion;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;

import java.io.FileNotFoundException;
import java.util.Map;

import com.google.common.collect.Lists;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.internal.QQAndroidBot;
import net.mamoe.mirai.internal.deps.okhttp3.internal.platform.Platform;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.auroralove.xiaoluo.mysql.MySQLManager;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.List;

public class Plugin extends JavaPlugin implements Listener{
    private static FileConfiguration config;
    public QQAndroidBot bot;
    public String config_prefixCheckKey;
    public String config_prefixUpdateKey;
    public String message_bot_nofindUsername;
    public String message_bot_Nobinding;
    public String config_prefixDeleteKey;
    public String message_check_sponsor;
    public String message_member_leave;
    public String message_update_success;
    MiraiEventHost host;
    Commands commands;
    public List<Runnable> onLogin = Lists.newArrayList();
    public String message_prefix;
    public String message_logining;
    public String message_loginfail;
    public String message_loginsuccess;
    public String message_logined;
    public String message_chatformat;
    public String message_destoryed;
    public String message_reloaded;
    public String message_playernotexist;
    public String message_bot_invalidUsername;
    public String message_bot_nosuchUsername;
    public String message_bot_getqq;
    public String message_bot_getid;
    public String message_bot_addsuccess;
    public String message_bot_already;
    public String message_bot_banned;
    public String message_bot_join;
    public String message_bot_SignFail;
    public String message_whitelist_kick;
    public String message_whitelist_max;

    public String message_remove_success;
    public String message_sql_fail;
    public long config_uid;
    public String config_password;
    public boolean config_auto_login;
    public boolean config_allowFriendRequest;
    public boolean config_autoAcceptFriendAddRequest;
    public boolean config_if_allow_update;
    public String config_nameRegex;
    public String config_passwordRegex;
    public String config_prefixCommandKey;
    public String config_protocol;
    public List<Long> config_groupList;
    public List<Long> config_blacklist;
    public long config_opgroup;
    public long config_chatgroup;
    public String message_noPermission;
    public String config_prefixReloadKey;

    public Plugin() {
    }

    public void loadPluginConfig() {
        this.saveDefaultConfig();
        this.message_prefix = this.getString("messages.prefix", "&7[&b极光小落&7]&e").replace("&", "§");
        this.message_logining = this.getString("messages.logining", "&e正在登录机器人 $uid &7(详细结果请见控制台)").replace("&", "§");
        this.message_loginfail = this.getString("messages.loginfail", "&c登录机器人 $uid 失败! &a原因: &7 $reason").replace("&", "§");
        this.message_loginsuccess = this.getString("messages.loginsuccess", "&a机器人 $uid 登录成功!").replace("&", "§");
        this.message_logined = this.getString("messages.logined", "&a机器人已经在线了! 无需重复登录").replace("&", "§");
        this.message_chatformat = this.getString("messages.chatformat", "&a机器人已经在线了! 无需重复登录").replace("&", "§");
        this.message_destoryed = this.getString("messages.destoryed", "&a机器人实例已销毁!").replace("&", "§");
        this.message_reloaded = this.getString("messages.reloaded", "&a插件重载完毕").replace("&", "§");
        this.message_noPermission = this.getString("messages.noPermission", "你没有权限").replace("&", "§");
        this.message_playernotexist = this.getString("messages.playernotexist", "&c玩家不存在").replace("&", "§");
        this.message_bot_invalidUsername = this.getString("messages.bot.invalidUsername", "无效的用户名");
        this.message_bot_nosuchUsername = this.getString("messages.bot.nosuchUsername", "无效的用户名");
        this.message_bot_nofindUsername = this.getString("messages.bot.nofindUsername", "无效的用户名");
        this.message_bot_Nobinding = this.getString("messages.bot.Nobinding", "您还暂未申请白名单，发送 申请白名单+游戏ID 以申请");
        this.message_bot_getqq = this.getString("messages.bot.getqq", "无效的用户名");
        this.message_bot_getid = this.getString("messages.bot.getid", "无效的用户名");
        this.message_bot_addsuccess = this.getString("messages.bot.addsuccess", "无效的用户名");
        this.message_bot_already = this.getString("messages.bot.already", "玩家 $user 已经是白名单了，请不要重复添加");
        this.message_bot_banned = this.getString("messages.bot.addfail-banned", "玩家 $user 已被封禁，无法添加到白名单").replace("&", "§");
        this.message_bot_join = this.getString("messages.bot.join", "玩家 $user 已被封禁，无法添加到白名单").replace("&", "§");
        this.message_whitelist_kick = this.getString("messages.whitelist.kick", "&b你还没有白名单，请加群 XXXX 到群内机器人申请").replace("&", "§");
        this.message_bot_SignFail = this.getString("messages.bot.SignFail","yoho！你今天已经签到过啦，请明天再来吧~");
        this.message_whitelist_max = this.getString("messages.whitelist.max", "&b你的QQ号已经不能再申请更多的白名单了!").replace("&", "§");
        this.message_check_sponsor = this.getString("messages.bot.sponsor", "");
        this.message_member_leave = this.getString("messages.bot.member_leave", "");
        this.message_sql_fail = this.getString("messages.bot.sql_fail", "数据库操作失败，请联系管理员处理");

        this.config_groupList = this.getConfig().getLongList("general.groupList");
        this.config_uid = this.getLong("general.qq", -1L);
        this.config_password = this.getString("general.password", "null");
        this.config_allowFriendRequest = this.getBoolean("general.autoAcceptFriendAddRequest", false);
        this.config_autoAcceptFriendAddRequest = this.getBoolean("general.autoAcceptFriendAddRequest", false);
        this.config_nameRegex = this.getString("general.nameRegex", "[a-zA-Z0-9_]{3,16}");
        this.config_passwordRegex = this.getString("general.passwordRegex", "[a-zA-Z0-9_]{}");
        this.config_prefixCommandKey = this.getString("general.prefixCommandKey", "申请白名单");
        this.config_prefixDeleteKey = this.getString("general.prefixDeleteKey", "删除白名单");
        this.config_prefixCheckKey = this.getString("general.prefixCheckKey", "查询白名单");
        this.config_prefixUpdateKey = this.getString("general.prefixUpdateKey", "更新白名单");
        this.config_prefixReloadKey = this.getString("general.prefixReloadKey", "重载白名单");
        this.config_protocol = this.getString("general.protocol", "ANDROID_PAD");
        this.config_auto_login = this.getBoolean("general.auto-login", true);
        this.config_opgroup = this.getLong("general.opgroup",686974095);
        this.config_chatgroup = this.getLong("general.chatgroup",773735730);
        this.config_blacklist = this.getConfig().getLongList("general.blacklist");
        this.config_if_allow_update = this.getConfig().getBoolean("general.if-allow-update",false);
        this.message_remove_success = this.getString("messages.whitelist.remove_success","|-\n" +
                "      成功移除白名单\n" +
                "      QQ: $qq\n" +
                "      ID: $ID");
        this.message_update_success = this.getString("messages.whitelist.update_success","|-\n" +
                "      成功更新白名单\n" +
                "      QQ: $qq\n" +
                "      ID: $ID");
        this.getLogger().info("插件配置载入完毕");
    }

    public void reloadConfig() {

        super.reloadConfig();
        this.loadPluginConfig();
        this.initBot();
        if (this.config_uid >= 10000L && this.config_auto_login) {
            this.getLogger().info(this.message_prefix + this.message_logining.replace("$uid", String.valueOf(this.config_uid)));
            this.login();
        }

    }
    public void onEnable() {

        this.commands = new Commands(this);
        this.host = new MiraiEventHost(this);
        ClassLoader old = Thread.currentThread().getClass().getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Platform.get().getClass().getClassLoader());

            this.addDefaultLoginTask();
            this.reloadConfig();


            if(!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File file=new File(getDataFolder(),"config.yml");
            if (!(file.exists())) {
                saveDefaultConfig();
            }
            reloadConfig();
            config = getConfig();
            //以上为创建配置文件文件夹和配置文件
            //利用BukkitRunnable创建新线程，防止使用SQL而堵塞主线程
            SendMessage("服务器正在开启中...");

            // Service loading.
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            Bukkit.getPluginManager().registerEvents(this.commands, this);
            Bukkit.getPluginManager().registerEvents(this, this);
        }
    }


    public void addDefaultLoginTask() {
        this.onLogin.add(() -> {
            Plugin.this.bot.getEventChannel().registerListenerHost(host);
            Plugin.this.getLogger().info("已注册默认的QQ监听事件");
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (MySQLManager.findData_by_name(event.getName())==null) {
            event.disallow(Result.KICK_WHITELIST, this.message_whitelist_kick.replace("$user", event.getName()));
        }
    }

    @EventHandler
    public void onPLayerMessage(AsyncPlayerChatEvent event) {
      if (!event.isCancelled()) {
          new BukkitRunnable() {
              @Override
              public void run() {
                  Player player = event.getPlayer();
                  if (player.hasPermission("chat.receive")) {
                      String regex = "&[1-9A-Fa-fK-Ok-oRr]?";
                      String regex2 = "%[1-9i]?";
                      String message = event.getMessage().replaceAll(regex, "").replaceAll(regex2, " [展示物品] ");
                      SendMessage(player.getName() + "：" + message);
                  }
              }
          }.runTaskAsynchronously(this);
      }
    }
    public void initBot(){
        try {
            this.getLogger().info("正在初始化机器人实例");
            String deviceInfoPath = this.getDataFolder().getAbsolutePath() + "\\deviceInfo.json";
            this.getLogger().info("设备信息地址："+deviceInfoPath);
            MiraiProtocol protocol = this.getProtocolFromString(MiraiProtocol.ANDROID_PAD);
            this.getLogger().info("使用协议: " + protocol.name());

            this.bot = (QQAndroidBot)BotFactory.INSTANCE.newBot(this.config_uid, BotAuthorization.byQRCode(), new BotConfiguration() {
                {
                    this.setProtocol(protocol);
                    this.fileBasedDeviceInfo(deviceInfoPath);
                }
            });
            this.getLogger().info("初始化完成");
        } catch (Throwable var3) {
            this.getLogger().warning("初始化机器人时出现一个异常: " + var3.getLocalizedMessage());
        }

    }

    public void login() {
        try {
            if (this.bot == null) {
                this.initBot();
            }

            this.bot.login();

            for (Runnable r : this.onLogin) {
                r.run();
            }
        } catch (Throwable var3) {
            this.getLogger().warning("登录机器人时出现一个异常: ");
            this.getLogger().warning(var3.getLocalizedMessage());
        }

    }
    public static String getConfigString(String path)
    {
        return config.getString(path);
    }
    public static int getConfigInt(String path)
    {
        return config.getInt(path);
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.commands.onCommand(sender, command, label, args);
        return true;
    }

    public void onDisable() {
        MySQLManager.get().shutdown(); //断开连接
        if (this.bot != null) {
            try {
                SendMessage("服务器已关闭，请等待稍后重启");
                this.bot.close(new Exception("服务器关闭，登出机器人"));
            } catch (Throwable var2) {
                this.getLogger().warning("卸载插件登出机器人时出现一个异常: " + var2.getLocalizedMessage());
            }

            this.getLogger().info("机器人账号已登出");
        }

        this.bot = null;
        this.host = null;
        this.commands = null;
        this.getLogger().info("插件已卸载");
    }

    public void SendMessage(String message){
        bot.getGroupOrFail(config_chatgroup).sendMessage(message);
    }

    public MiraiProtocol getProtocolFromString(MiraiProtocol nullValue) {
        MiraiProtocol[] var3 = MiraiProtocol.values();

        for (MiraiProtocol p : var3) {
            if (p.name().equalsIgnoreCase(this.config_protocol)) {
                return p;
            }
        }

        return nullValue;
    }

    public long getLong(String key, long nullValue) {
        if (this.getConfig().contains(key)) {
            return this.getConfig().getLong(key);
        } else {
            this.getLogger().warning("无法在配置文件中找到长整型值 \"" + key + "\"，将使用默认值");
            return nullValue;
        }
    }

    public int getInt(String key, int nullValue) {
        if (this.getConfig().contains(key)) {
            return this.getConfig().getInt(key);
        } else {
            this.getLogger().warning("无法在配置文件中找到整数型值 \"" + key + "\"，将使用默认值");
            return nullValue;
        }
    }

    public boolean getBoolean(String key, boolean nullValue) {
        if (this.getConfig().contains(key)) {
            return this.getConfig().getBoolean(key);
        } else {
            this.getLogger().warning("无法在配置文件中找到布尔型值 \"" + key + "\"，将使用默认值");
            return nullValue;
        }
    }

    public String getString(String key, String nullValue) {
        if (this.getConfig().contains(key)) {
            return this.getConfig().getString(key);
        } else {
            this.getLogger().warning("无法在配置文件中找到文本型值 \"" + key + "\"，将使用默认值");
            return nullValue;
        }
    }
    public String dispatchCommand(CommandSender sender, String commandLine) {
        StringBuilder commandResult = new StringBuilder();
        CommandSender proxyCommandSender = (CommandSender) Proxy.newProxyInstance(
                this.getClassLoader(), // this 指的是 JavaPlugin 不是 this.getClass().getClassLoader();
                new Class[]{CommandSender.class},
                (proxy, method, args) -> {
                    if(method.getName().equals("sendMessage") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == String.class) {
                        commandResult.append(args[0]);
                    }
                    return method.invoke(sender, args);
                }
        );
        Bukkit.dispatchCommand(proxyCommandSender, commandLine);
        String regex = "§[1-9A-Fa-fK-Ok-oRr]?";
        return commandResult.toString().replaceAll(regex,"");
    }
}
