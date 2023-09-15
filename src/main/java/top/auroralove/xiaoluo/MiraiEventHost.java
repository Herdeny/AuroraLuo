package top.auroralove.xiaoluo;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.auroralove.xiaoluo.mysql.MySQLManager;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNumeric;

public class MiraiEventHost extends SimpleListenerHost {
    Plugin main;
    Commands commands;

    public MiraiEventHost(Plugin main) {
        this.main = main;
    }


    @EventHandler
    private ListeningStatus onNewFriendRequest(NewFriendRequestEvent event) {
        if (this.main.config_autoAcceptFriendAddRequest) {
            event.accept();
        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onMemberJoinEvent(MemberJoinEvent event) {
        event.getGroup().sendMessage((new At(event.getMember().getId())).plus(this.main.message_bot_join));
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onMemberJoinRequestEvent(MemberJoinRequestEvent event) {
        if (!this.main.config_blacklist.contains(event.getFromId())) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("在处理加群事件时出现了一个异常" + e.getLocalizedMessage());
            }
            event.accept();
            return ListeningStatus.LISTENING;
        } else event.reject(true, "您的账号位于服务器黑名单中");
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onMemberLeaveEvent(MemberLeaveEvent event) {
        String qq = String.valueOf(event.getMember().getId());
        if (checkWhitelist(1, qq) != null) {
            BigDecimal sponsor_total, sponsor_this, sponsor_return;
            String is_svipp = MySQLManager.is_svipp(qq) == 0 ? "否" : "是";
            String is_ssvip = MySQLManager.is_ssvip(qq) == 0 ? "否" : "是";
            String player_name = checkWhitelist(1, qq);
            String start_date = String.valueOf(MySQLManager.get_start_date(qq));
            sponsor_total = MySQLManager.get_personal_total_sponsor(qq);
            sponsor_this = MySQLManager.get_personal_this_sponsor(qq);
            sponsor_return = Objects.requireNonNull(MySQLManager.get_return_sponsor(qq)).multiply(BigDecimal.valueOf(100));
            String leave_message = this.main.message_member_leave.replace("$sponsor_total", String.valueOf(sponsor_total)).replace("$sponsor_this", String.valueOf(sponsor_this)).replace("$sponsor_return", String.valueOf(sponsor_return)).replace("$is_svipp", is_svipp).replace("$is_ssvip", is_ssvip).replace("$qq", qq).replace("$id", player_name).replace("$start", start_date);
            if (MySQLManager.set_leave_true(String.valueOf(event.getMember().getId())) == -1) {
                leave_message += "\n离开信息已记录";
            } else leave_message += "离开信息记录失败";
            this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage(leave_message);
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onBotOnline(BotOnlineEvent event) {
        this.main.getLogger().info(this.main.message_prefix + this.main.message_loginsuccess.replace("$uid", String.valueOf(event.getBot().getId())));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (MySQLManager.enableMySQL() == -1) {
                    main.getLogger().info("数据库已连接");
                }
            }
        }.runTaskAsynchronously(main);
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onBotRelogin(BotReloginEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (MySQLManager.enableMySQL() == -1) {
                    main.getLogger().info("数据库已连接");
                }
            }
        }.runTaskAsynchronously(main);
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onFriendMessage(FriendMessageEvent event) {

        String msg = event.getMessage().contentToString();
        String sender = String.valueOf(event.getSender().getId());//发送者的QQ
        String playerid = MySQLManager.findData_by_qq(String.valueOf(event.getSender().getId()));//发送者的ID

        String key = "修改密码";
        String key_sponsor = "档案";

        if (event.getSender().getId() == this.main.bot.getId()) return ListeningStatus.LISTENING;
        if (playerid != null) {
            String command;
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerid).getBytes(StandardCharsets.UTF_8));//发送者的UUID
            Player player = Bukkit.getPlayer(uuid);

            if (msg.startsWith(key)) {
                if (msg.length() > key.length()) {
                    String password = msg.substring(key.length()).trim();
                    if (isMatchPassword(password)) {
                        command = "authme password " + playerid + " " + password;
                        String returnmessage = this.main.dispatchCommand(Bukkit.getConsoleSender(), command);
                        if (!returnmessage.isEmpty()) {
                            event.getSender().sendMessage(returnmessage);
                        } else
                            event.getSender().sendMessage("修改成功！游戏账户 " + playerid + " 的新密码是：" + password + "\n\nPS:如果您的游戏账户尚未注册，那么修改密码是无效的，请先登录服务器注册");
                    } else event.getSender().sendMessage("密码只允许为5-30位大小写字母、数字、下划线的组合");
                } else event.getSender().sendMessage("正确格式：\n修改密码 新密码");
                return ListeningStatus.LISTENING;
            } else if (msg.contains("密码")) {
                event.getSender().sendMessage("您是否想要修改密码？\n如要修改密码请发送“修改密码 新密码”");
                return ListeningStatus.LISTENING;
            }

            if (msg.equals("登录") || msg.equals("登陆")) {
                command = "authme forcelogin " + playerid;
                if (this.main.dispatchCommand(Bukkit.getConsoleSender(), command).contains("performed")) {
                    event.getSender().sendMessage("登录成功！祝您游戏愉快~");
                    return ListeningStatus.LISTENING;
                } else event.getSender().sendMessage("快捷登录失败，请在游戏内输入密码登录");
                return ListeningStatus.LISTENING;
            } else if (msg.contains("登")) {
                event.getSender().sendMessage("您是否想要知道快捷登录的方法？\n发送“登录”即可跳过输入密码直接登录");
                return ListeningStatus.LISTENING;
            }
            if (msg.equals("下线")) {
                if (player == null) {
                    event.getSender().sendMessage("您的账户处于离线状态");
                    return ListeningStatus.LISTENING;
                }
                command = "kick " + playerid + " 来自移动端的强制下线操作";
                this.main.dispatchCommand(Bukkit.getConsoleSender(), command);
                event.getSender().sendMessage("强制下线成功！");
                return ListeningStatus.LISTENING;
            } else if (msg.contains("踢") || msg.contains("下") || msg.contains("出")) {
                event.getSender().sendMessage("您是否想要强制下线？\n发送“下线”即可使您的游戏账号强制下线");
                return ListeningStatus.LISTENING;
            }
            if (msg.equals(key_sponsor)) {
                BigDecimal sponsor_total, sponsor_this, sponsor_return;
                String is_svipp = MySQLManager.is_svipp(sender) == 0 ? "否" : "是";
                String is_ssvip = MySQLManager.is_ssvip(sender) == 0 ? "否" : "是";
                String is_online = (player == null) ? "离线" : "在线";
                String start_date = String.valueOf(MySQLManager.get_start_date(sender));
                sponsor_total = MySQLManager.get_personal_total_sponsor(sender);
                sponsor_this = MySQLManager.get_personal_this_sponsor(sender);
                sponsor_return = Objects.requireNonNull(MySQLManager.get_return_sponsor(sender)).movePointRight(2);
                event.getSender().sendMessage(this.main.message_check_sponsor.replace("$sponsor_total", String.valueOf(sponsor_total)).replace("$sponsor_this", String.valueOf(sponsor_this)).replace("$sponsor_return", String.valueOf(sponsor_return)).replace("$is_svipp", is_svipp).replace("$is_ssvip", is_ssvip).replace("$start", start_date).replace("$qq", sender).replace("$id", playerid).replace("$online", is_online));
                return ListeningStatus.LISTENING;
            }
            if (msg.contains("赞助") || msg.contains("充值")) {
                event.getSender().sendMessage("感谢您的赞助与支持~您的游戏ID将被记录在寻梦之旅的赞助榜单中！\n" +
                        "\uD83D\uDCAB赞助比例：0.95 RMB = 100 点券\n" +
                        "\uD83D\uDC47扫描下方二维码即可进行赞助 微信或支付宝都可以哦\n" +
                        "⚡旅行者记得备注自己的游戏ID哦~管理员看到后会立即将点券发送到您的游戏账号中\n" +
                        "如点券长时间未到账/忘记备注ID/其他问题 请及时联系我们 我们会立即为您解决~");
             ExternalResource externalResource = ExternalResource.create(new File(this.main.getDataFolder(),"zanzhu.png"));
             Image i = event.getFriend().uploadImage(externalResource);
             event.getFriend().sendMessage(i);
             this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("玩家 "+ playerid + "(QQ:"+ sender +") 查询了赞助信息，请留意后台资金到账");
            return ListeningStatus.LISTENING;
            }

        } else event.getSender().sendMessage(this.main.message_bot_Nobinding + "\n(请在群中申请白名单)");

        return ListeningStatus.LISTENING;
    }

    @EventHandler
    private ListeningStatus onGroupMessage(GroupMessageEvent event) {
        if (this.main.config_groupList.contains(event.getGroup().getId())) {
            String msg_mirai = event.getMessage().serializeToMiraiCode(); //将消息转化为mirai码的形式  [mirai:TYPE:PROP]，其中 TYPE 为消息类型, PROP 为属性
            String msg = event.getMessage().contentToString(); //QQ 对话框中以纯文本方式会显示的消息内容。无法用纯文字表示的消息会丢失信息，如任何图片都是 [图片]
            String key_add = this.main.config_prefixCommandKey.toLowerCase(); //申请白名单的key信息
            String key_check = this.main.config_prefixCheckKey.toLowerCase(); //查询白名单的key信息
            String key_delete = this.main.config_prefixDeleteKey.toLowerCase(); //删除白名单的key信息
            String key_update = this.main.config_prefixUpdateKey.toLowerCase();//更新白名单的key信息
            String key_reload = this.main.config_prefixReloadKey.toLowerCase();//重载白名单的key信息
            String sender = String.valueOf(event.getSender().getId());//取信息发送者的QQ
            int sender_level = event.getSender().getPermission().getLevel();//取消息发送者的权限
            String key_recharge = "/cz";
            boolean At = event.getMessage().serializeToMiraiCode().contains("[mirai:at:");
            if (event.getGroup().getId() == this.main.config_opgroup) {
                if (msg.startsWith("/")) {
                    if (sender_level != 0) {
                        if (msg.startsWith(key_recharge)) {
                            String[] arr = msg.split(" ");
                            if (isNumeric(arr[1]) && isNumeric(arr[2])) {
                                String player_name = checkWhitelist(1, arr[1]);
                                if (player_name != null) {
                                    commands.Recharge(player_name, Integer.parseInt(arr[2]), String.valueOf(event.getSender().getId()));
                                } else event.getGroup().sendMessage("该玩家尚未绑定白名单，无法进行赞助");
                            } else event.getGroup().sendMessage("请发送正确的格式！\n/充值 QQ 数额(单位：点券)");
                        } else
                            event.getGroup().sendMessage(this.main.dispatchCommand(Bukkit.getConsoleSender(), msg.substring(1)));
                    } else event.getGroup().sendMessage(this.main.message_noPermission);
                }
                return ListeningStatus.LISTENING;
            }
            if (msg.toLowerCase().startsWith(key_add)) {//触发添加白名单关键词
                String player = msg.substring(key_add.length()).trim(); //取要设置的id
                switch (addWhitelist(sender, player)) {
                    //返回代码：
                    //-1 申请成功
                    //0 名称不符合玩家规范
                    //1 名称存在服务器ban表中
                    //2 玩家已申请过白名单
                    //3 名称已被占用
                    //4 格式错误
                    //5 其他错误
                    case -1:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_addsuccess.replace("$user", player)));//发送修改成功信息
                        Objects.requireNonNull(event.getGroup().get(event.getSender().getId())).setNameCard(player);//修改群名片
                        Date date= Date.valueOf(LocalDate.now());
                        MySQLManager.set_start_date(sender,date);
                        break;
                    case 0:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_invalidUsername.replace("$name", player))); //发送提示信息
                        break;
                    case 1:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_banned.replace("$user", player)));//发送提示信息
                        break;
                    case 2://取库中已存在的ID
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_whitelist_max.replace("$user", checkWhitelist(1, sender))));//发送提示信息
                        break;
                    case 3:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_already.replace("$user", player)));//发送提示信息
                        break;
                    default:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus("申请失败，请联系管理员"));
                        break;
                }
                return ListeningStatus.LISTENING;
            }

            if (msg.toLowerCase().startsWith(key_check)) {//触发查询白名单关键词
                if (At || msg.trim().length() <= key_check.length()) {//如果有@的人
                    String QQ = At ? getAtQQ(msg_mirai) : sender;//如果有被At，取被At的QQ，否则取发送者的QQ
                    String player = checkWhitelist(1, QQ);
                    if (player == null) {
                        event.getGroup().sendMessage(At ? (new QuoteReply(event.getSource())).plus(this.main.message_bot_nofindUsername.replace("$qq", QQ)) : new At(Long.parseLong(sender)).plus(this.main.message_bot_Nobinding));
                        return ListeningStatus.LISTENING;
                    }
                    //返回代码
                    //-1 成功
                    //0 未查询到
                    //2 查询失败
                    if (player.equals("2")) {
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus("查询失败！"));
                    } else {
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_getid.replace("$qq", QQ) + player));
                    }
                } else {//如果没有@的人
                    String player = msg.substring(key_check.length()).trim();//取要查询的id
                    String QQ = checkWhitelist(2, player);
                    switch (QQ) {
                        //返回代码
                        //-1 成功
                        //0 玩家名称不规范
                        //1 未查询到
                        //2 查询失败
                        case "-1":
                            event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_getqq.replace("$user", player) + QQ));//发送查询结果
                            break;
                        case "0":
                            event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_invalidUsername.replace("$name", player)));//发送错误信息
                            break;
                        case "1":
                            event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_nosuchUsername.replace("$user", player)));//发送错误信息
                            break;
                        case "2":
                            event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus("查询失败！"));
                            break;

                    }
                }
                return ListeningStatus.LISTENING;
            }

            if (msg.toLowerCase().startsWith(key_delete)) {//触发删除白名单关键词
                if (At || msg.trim().length() <= key_delete.length()) {//有At的人或关键词后无内容
                    String qq = At ? getAtQQ(msg_mirai) : sender;
                    String player = checkWhitelist(1, qq);
                    switch (deleteWhitelist(0, sender_level, qq)) {
                        //返回代码
                        //-1 成功
                        //0 玩家名称不规范
                        //1 无权限强制删除
                        //2 数据库操作失败
                        case -1:
                            if (MySQLManager.refresh_number() == -1) {
                                this.main.getLogger().info("已重置序列号");
                            }
                            event.getGroup().sendMessage(this.main.message_remove_success.replace("$qq", qq).replace("$ID", player));
                            break;
                        case 0:
                            event.getGroup().sendMessage(this.main.message_bot_invalidUsername.replace("$name", player));
                            break;
                        case 1:
                            event.getGroup().sendMessage(this.main.message_noPermission);
                            break;
                        case 2:
                            event.getGroup().sendMessage(this.main.message_sql_fail);
                            break;
                    }
                } else {
                    String player = msg.substring(key_delete.length()).trim();
                    String qq = String.valueOf(checkWhitelist(2, player));
                    switch (deleteWhitelist(1, sender_level, player)) {
                        //返回代码
                        //-1 成功删除
                        //0 玩家名称不符合规范
                        //1 没有权限
                        //2 数据库操作失败
                        case -1:
                            event.getGroup().sendMessage(this.main.message_remove_success.replace("$qq", qq).replace("$ID", player));
                            break;
                        case 0:
                            event.getGroup().sendMessage(this.main.message_bot_invalidUsername.replace("$name", player));
                            break;
                        case 1:
                            event.getGroup().sendMessage(this.main.message_noPermission);
                            break;
                        case 2:
                            event.getGroup().sendMessage(this.main.message_sql_fail);
                            break;
                    }
                }

                return ListeningStatus.LISTENING;
            }
            //更新白名单
            if (msg.toLowerCase().startsWith(key_update)) {
                String qq, name;
                int type_level;
                if (At && !Objects.equals(getAtQQ(msg_mirai), sender)) {
                    qq = getAtQQ(msg_mirai);
                    type_level = 1;
                    name = msg_mirai.substring(msg_mirai.indexOf(']')).trim();

                } else {
                    qq = sender;
                    type_level = 0;
                    name = msg.substring(key_update.length()).trim();
                }
                this.main.getLogger().info("执行更改白名单操作\nqq:" + qq + "\nname:" + name);
                //返回代码
                //-1 成功
                //0 玩家名称不符合规范
                //1 欲更改ID在ban表中
                //2 欲更改ID已被占用
                //3 目前不允许更改ID
                //4 没有权限
                //5 数据库错误
                switch (updateWhitelist(type_level, sender_level, qq, name)) {
                    case -1:
                        event.getGroup().sendMessage(this.main.message_update_success.replace("$qq", qq).replace("$ID", name));
                        Objects.requireNonNull(event.getGroup().get(event.getSender().getId())).setNameCard(name);//修改群名片
                        break;
                    case 0:
                        event.getGroup().sendMessage(this.main.message_bot_invalidUsername.replace("$name", name));
                        break;
                    case 1:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_banned.replace("$user", name)));//发送提示信息
                        break;
                    case 2:
                        event.getGroup().sendMessage((new QuoteReply(event.getSource())).plus(this.main.message_bot_already.replace("$user", name)));//发送提示信息
                        break;
                    case 3:
                        event.getGroup().sendMessage("服务器当前不允许更改白名单\n更改白名单权限一般会在周目开启前开放，具体请留意公告通知");
                        break;
                    case 4:
                        event.getGroup().sendMessage("您没有权限为他人更改白名单，请联系本人或管理员操作");
                        break;
                    case 5:
                        event.getGroup().sendMessage(this.main.message_sql_fail);
                }
                return ListeningStatus.LISTENING;
            }

            if (msg.toLowerCase().startsWith(key_reload)) {
                if (sender_level != 0) {
                    this.main.reloadConfig();
                } else event.getGroup().sendMessage(this.main.message_noPermission);
            }

            if (msg.contains("在线") || msg.contains("状态") || msg.contains("多少人") || msg.contains("几个人")) {
                List blackword = onlineplayer();
                event.getGroup().sendMessage("服务器运行状态：正常" + "\n" + "当前在线：" + blackword.size() + " 人" + "\n" + "在线玩家:\n" + blackword);
                return ListeningStatus.LISTENING;
            }

            String playerid = checkWhitelist(1, sender);
            if (playerid != null && isMatchPlayerName(playerid)) {
                if (msg.equals("[语音消息]") || msg.equals("[闪照]") || msg.equals("[转发消息]") || msg.equals("[动画表情]")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", msg).replace("[", "§7§o["));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("[mirai:service:")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", "§7§o[服务消息]"));
                    return ListeningStatus.LISTENING;
                } else if (msg.contains("[分享]")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", msg).replace("[分享", "§7§o[分享"));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("[mirai:file:")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", "§7§o[上传文件]"));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("[mirai:app:") || msg.contains("{\"app\":")) {
                    if (msg_mirai.contains("prompt")) {
                        String prompt = prompt(msg_mirai);
                        SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", "§7§o" + prompt));
                        return ListeningStatus.LISTENING;
                    }
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", "§7§o[小程序分享]"));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("[骰子:")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", "§7§o[掷骰子]"));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("[mirai:image:")) {
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", msg).replace("[图片]", "§7§o[图片]"));
                    return ListeningStatus.LISTENING;
                } else if (msg_mirai.contains("不支持的消息"))
                    return ListeningStatus.LISTENING;

                if (At) {
                    String qq = getAtQQ(msg_mirai);
                    String player = checkWhitelist(1, qq);
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", msg).replace(qq, player + "§r").replace("@", "§7§o@"));
                    return ListeningStatus.LISTENING;
                } else
                    SendMessage("sbc perm=chat.receive " + this.main.message_chatformat.replace("%player%", playerid).replace("%message%", msg).replace("@全体成员", "§6§o@全体成员 §r"));
                return ListeningStatus.LISTENING;
            }
            return ListeningStatus.LISTENING;
        }
        return ListeningStatus.LISTENING;
    }


    public boolean isMatchPlayerName(String player) {
        Pattern p = Pattern.compile(this.main.config_nameRegex);
        Matcher m = p.matcher(player);
        return m.matches();
    }

    public boolean isMatchPassword(String password) {
        Pattern p = Pattern.compile(this.main.config_passwordRegex);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    public String getAtQQ(String msg) {
        String before = "[mirai:at:";
        String after = "]";
        int start = msg.indexOf(before);
        int end = msg.indexOf(after, start + before.length());
        return msg.substring(start + before.length(), end);
    }

    public String prompt(String msg) {
        String before = "\"prompt\":\"";
        String after = "\"";
        int start = msg.indexOf(before);
        int end = msg.indexOf(after, start + before.length());
        return msg.substring(start + before.length(), end);
    }

    public int addWhitelist(String sender, String player) {
        if (player != null) { //如果申请白名单后面有内容

            if (this.isMatchPlayerName(player)) {//检查是否符合玩家名称规范

                //第一步
                for (OfflinePlayer ban : Bukkit.getBannedPlayers()) {//检查服务器ban表
                    if (ban.getName().equalsIgnoreCase(player)) {//如果在ban表中
                        return 1;//返回
                    }
                }//未被ban则继续

                //第二步
                if (checkWhitelist(1, sender) != null) {//如果已经申请白名单了
                    return 2;//返回
                }//未申请过则继续

                //第三步
                if (MySQLManager.findData_by_name(player) != null) {//如果id已经被申请了
                    return 3;//返回
                }//未被申请则继续
                return MySQLManager.insertData(sender, player);

            } //如果不符合玩家名称规范
            else return 0;

        } //如果白名单后无内容
        else return 0;
    }

    public String checkWhitelist(int type, String object) {
        switch (type) {
            case 1://有id返回id 没id返回null
                return MySQLManager.findData_by_qq(object);
            case 2:
                if (this.isMatchPlayerName(object)) {//如果符合玩家规范
                    String qq = MySQLManager.findData_by_name(object);
                    if (qq != null) {//如果此id存在在白名单中
                        return qq;
                    }//如果不在白名单中
                    else return "1";
                } //如果不符合玩家名称规范
                else return "0";
        }
        return "2";
    }

    public int deleteWhitelist(int type_delete, int level, String object) {//type_delete:0QQ 1ID
        if (level == 0) {
            return 1;
        }
        switch (type_delete) {
            case 0:
                return MySQLManager.deleteData_by_qq(object);
            case 1:
                if (isMatchPlayerName(object)) {
                    return MySQLManager.deleteData_by_name(object);
                } else return 0;
        }
        return 2;
    }

    public int updateWhitelist(int type_update, int level, String qq, String name) {//type_update: 0-为本qq更改白名单 1-为他人qq更改白名单
        if (type_update == 0) {//为本QQ更改白名单
            if (this.main.config_if_allow_update) {//如果允许更改白名单
                return changeID(name, qq);//执行更改操作并返回结果
            } else return 3;//不允许更改白名单
        } else if (level != 0) {
            return changeID(name, qq);
        } else return 4;//无权限为他人更改白名单
    }

    public int changeID(String qq, String name) {
        if (name != null) { //如果申请白名单后面有内容

            if (this.isMatchPlayerName(name)) {//检查是否符合玩家名称规范

                //第一步
                for (OfflinePlayer ban : Bukkit.getBannedPlayers()) {//检查服务器ban表
                    if (ban.getName().equalsIgnoreCase(name)) {//如果在ban表中
                        return 1;//返回
                    }
                }//未被ban则继续

                //第二步
                if (MySQLManager.findData_by_name(name) != null) {//如果id已经被申请了
                    return 2;//返回
                }//未被申请则继续
                return MySQLManager.updateData_by_qq(qq, name);
            } //如果不符合玩家名称规范
            else return 0;

        } //如果白名单后无内容
        else return 0;
    }

    public void SendMessage(String message) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }

    public List<String> onlineplayer() {
        LinkedList online = new LinkedList();
        Collection<? extends Player> onlineplayer = Bukkit.getOnlinePlayers();

        for (Player p : onlineplayer) {
            online.add(p.getName());
        }

        return online;
    }
}