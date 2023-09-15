package top.auroralove.xiaoluo;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import top.auroralove.xiaoluo.mysql.MySQLManager;

import java.math.BigDecimal;
import java.util.Objects;

import static org.apache.commons.lang.StringUtils.isNumeric;

public class Commands implements Listener {
    Plugin main;

    public Commands(Plugin main) {
        this.main = main;
    }

    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean access = sender.isOp();
        if (args[0].equalsIgnoreCase("reload")) {
            if (access) {
                this.main.saveDefaultConfig();
                this.main.reloadConfig();
                return;
            } else {
                showHelp(sender);
            }
            return;
        }
        if (args[0].equalsIgnoreCase("on")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " permission set chat.receive true");
            sender.sendMessage("§7[§f极光小落§7] §e已开始接受群聊消息");
            return;
        }
        if (args[0].equalsIgnoreCase("off")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " permission set chat.receive false");
            sender.sendMessage("§7[§f极光小落§7] §c已停止接受群聊消息");
            return;
        }
        if (args[0].equalsIgnoreCase("send")) {
            if (sender.hasPermission("xiaoluo.sendmessage")) {
                if (args.length >= 2) {
                    String message = args[1];
                    this.main.SendMessage(message);
                } else sender.sendMessage("§7[§f极光小落§7] §c请输入有效的内容！");
            } else showNoPermission(sender);
            return;
        }
        if (args[0].equalsIgnoreCase("recharge")) {
            if (access) {
                if (args.length == 3) {
                    String player_name = args[1];
                    int amount;
                    if (!player_name.matches(this.main.config_nameRegex)) {
                        sender.sendMessage("§7[§f极光小落§7] §c玩家名不合法！");
                        return;
                    }
                    if (isNumeric(args[2])) {
                        amount = Integer.parseInt(args[2]);
                    } else {
                        sender.sendMessage("§7[§f极光小落§7] §c点券必须为数字");
                        return;
                    }
                    Recharge(player_name, amount, sender.getName());
                } else
                    sender.sendMessage("§7[§f极光小落§7] §c参数错误！\n§e正确格式 /xiaoluo recharge <玩家ID> <点券数量>");
            } else showNoPermission(sender);
        } else showHelp(sender);

    }

    public void showHelp(CommandSender sender) {
        if (!sender.isOp()) {
            sender.sendMessage("§7[§f极光小落§7] §e帮助:\n§b/chat off §f- §a不再接收群聊消息，不再向群聊发送消息\n§b/chat on §f- §a开始接收群聊消息，开始向群聊发送消息");
        } else
            sender.sendMessage("§7[§f极光小落§7] §e帮助:\n§b/chat off §f- §a不再接收群聊消息，不再向群聊发送消息\n§b/chat on §f- §a开始接收群聊消息，开始向群聊发送消息\n§e/chat recharge [点券数量]§f- §b为玩家充值指定数量的点券");
    }

    public void showNoPermission(CommandSender sender) {
        sender.sendMessage("§7[§f极光小落§7] §c您没有权限！");
    }

    public void Recharge(String player_name, int amount, String operator) {
        this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("正在进行赞助操作\n赞助玩家：" + player_name + "\n赞助金额：" + amount + "\n操作者：" + operator);
        String player_qq = MySQLManager.findData_by_name(player_name);
        BigDecimal[] RechargeTap = {BigDecimal.valueOf(0), BigDecimal.valueOf(1000), BigDecimal.valueOf(3000), BigDecimal.valueOf(6000), BigDecimal.valueOf(10000), BigDecimal.valueOf(20000), BigDecimal.valueOf(50000), BigDecimal.valueOf(80000), BigDecimal.valueOf(100000), BigDecimal.valueOf(999999999)};
        BigDecimal nowTap, nextTap;
        int i;
        if (player_qq != null) {
            if (!player_name.equals("fail1")) {
                if (this.main.dispatchCommand(Bukkit.getConsoleSender(), "wpa give " + player_name + " " + amount).startsWith("充值成功")) {
                    double temp_vault = amount * 1.0 / 100;
                    BigDecimal sponsor = new BigDecimal(String.valueOf(temp_vault));
                    if (MySQLManager.add_sponser_to_qq(player_qq, sponsor) == -1) {
                        BigDecimal sponsor_this = Objects.requireNonNull(MySQLManager.get_personal_this_sponsor(player_qq)).movePointRight(2);
                        for (i = 0; i < 8; i++) {
                            if (sponsor_this.compareTo(RechargeTap[i]) < 0) {
                                break;
                            }
                        }
                        nowTap = RechargeTap[i - 1];
                        nextTap = RechargeTap[i];
                        this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("成功为 $playerid 赞助 $amount 点券，数据库已记录".replace("$playerid", player_name).replace("$amount", String.valueOf(amount)));
                        this.main.bot.getFriendOrFail(Long.parseLong(player_qq)).sendMessage("感谢您的支持\uD83D\uDC90\uD83D\uDC90\uD83D\uDC90\n" + amount +
                                " 点券已发送到游戏账户 " + player_name + " 中 请旅行者查收~\n" +
                                "本周目您已赞助" + sponsor_this + "点券 可领取 " + nowTap + " 点券累充礼包\n" +
                                "距离下一阶段奖励还有 " + nextTap.subtract(sponsor_this) + "点券");
                    } else this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("数据库写入失败");
                } else
                    this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("未获取到赞助结果，请查看服务后台");
            } else
                this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("数据库连接失败，请检查数据库连接");
        } else
            this.main.bot.getGroupOrFail(this.main.config_opgroup).sendMessage("未在数据库中查询到玩家信息，请确认充值玩家是否正确");
    }
}
