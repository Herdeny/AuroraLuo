package top.auroralove.xiaoluo.mysql;

import java.math.BigDecimal;
import java.sql.*;

import static top.auroralove.xiaoluo.Plugin.getConfigInt;
import static top.auroralove.xiaoluo.Plugin.getConfigString;

public class MySQLManager {
    private static String ip;
    private static String databaseName;
    private static String userName;
    private static String userPassword;
    private static Connection connection;
    private static int port;
    public static MySQLManager instance = null;

    public static MySQLManager get() {
        return instance == null ? instance = new MySQLManager() : instance;
    }

    public static int enableMySQL()
    {
        ip = getConfigString("mysql.ip");
        databaseName = getConfigString("mysql.databasename");
        userName = getConfigString("mysql.username");
        userPassword = getConfigString("mysql.password");
        port = getConfigInt("mysql.port");
        connectMySQL();
        String cmd = SQLCommand.CREATE_TABLE1.commandToString();
        try {
            PreparedStatement ps = connection.prepareStatement(cmd);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void connectMySQL()
    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + databaseName + "?useSSL=false&autoReconnect=true&serverTimezone=GMT", userName, userPassword);
        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
    public static void doCommand(PreparedStatement ps)
    {
        try {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("执行指令失败，以下为错误提示");
            e.printStackTrace();
        }
    }
    public void shutdown() {
        try {
            connection.close();
        } catch (SQLException e) {
            //断开连接失败
            e.printStackTrace();
        }
    }

    public static int insertData(String data1, String data2) {
        try {
            enableMySQL();
            PreparedStatement ps;
            String s = SQLCommand.ADD_DATA.commandToString();
            ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(data1));
            ps.setString(2, data2);
            doCommand(ps);
            ps.close();
            connection.close();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5;
    }
    public static int deleteData_by_qq(String data1) {
        try {
            enableMySQL();
            PreparedStatement ps;
            String s = SQLCommand.DELETE_DATA_BY_QQ.commandToString();
            ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(data1));
            doCommand(ps);
            ps.close();
            connection.close();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2;
    }
    public static int deleteData_by_name(String data1) {
        try {
            enableMySQL();
            PreparedStatement ps;
            String s = SQLCommand.DELETE_DATA_BY_NAME.commandToString();
            ps = connection.prepareStatement(s);
            ps.setString(1, data1);
            doCommand(ps);
            ps.close();
            connection.close();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2;
    }
    public static int updateData_by_qq(String player_name,String player_qq) {
        try {
            enableMySQL();
            PreparedStatement ps;
            String s = SQLCommand.UPDATE_NAME_BY_QQ.commandToString();
            ps = connection.prepareStatement(s);
            ps.setString(1, player_name);
            ps.setLong(2, Long.parseLong(player_qq));
            doCommand(ps);
            ps.close();
            connection.close();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 5;
    }
    public static int updateData_by_name(String player_qq,String player_name) {
        try {
            enableMySQL();
            PreparedStatement ps;
            String s = SQLCommand.UPDATE_QQ_BY_NAME.commandToString();
            ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(player_qq));
            ps.setString(2, player_name);
            doCommand(ps);
            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static int refresh_number(){
        try {
            enableMySQL();
            PreparedStatement ps1,ps2;
            String s1 = SQLCommand.DELETE_NUMBER.commandToString();
            String s2 = SQLCommand.ADD_NUMBER.commandToString();
            ps1 = connection.prepareStatement(s1);
            ps2 = connection.prepareStatement(s2);
            doCommand(ps1);
            doCommand(ps2);
            ps1.close();
            ps2.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static String findData_by_qq(String qq) {
        String playername;
        try {
            enableMySQL();
            String s = SQLCommand.SELECT_DATA_BY_QQ.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);

            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                playername = rs.getString("user_playername");
                rs.close();
                ps.close();
                connection.close();
                return playername;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return "fail1";
        }
        return null;
    }
    public static String findData_by_name(String playername) {
        String str;
        try {
            enableMySQL();
            String s = SQLCommand.SELECT_DATA_BY_NAME.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setString(1, playername);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                str = rs.getString("user_qq");
                rs.close();
                ps.close();
                connection.close();
                return str;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO 自动生成的 catch 块
            return "fail";
        }
    return null;}
    public static BigDecimal get_total_sponsor_this(){
        BigDecimal total;
        try {
            enableMySQL();
            String s = SQLCommand.ADD_SPONSER_THIS_IN_TOTAL.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                total = rs.getBigDecimal(1);
                rs.close();
                ps.close();
                connection.close();
                return total;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO 自动生成的 catch 块
        }
        return null;
    }
    public static int add_sponser_to_qq(String qq,BigDecimal sponsor_amonut){
        BigDecimal sponsor_personal_total_new=sponsor_amonut.add(get_personal_total_sponsor(qq));
        BigDecimal sponsor_personal_this_new=sponsor_amonut.add(get_personal_this_sponsor(qq));
        try {
            enableMySQL();
            PreparedStatement ps,ps2;
            String s = SQLCommand.ADD_PERSONAL_TOTAL_SPONSER_TO_QQ.commandToString();
            String s2 = SQLCommand.ADD_PERSONAL_THIS_SPONSER_TO_QQ.commandToString();
            ps = connection.prepareStatement(s);
            ps2 = connection.prepareStatement(s2);
            ps.setBigDecimal(1, sponsor_personal_total_new);
            ps2.setBigDecimal(1, sponsor_personal_this_new);
            ps.setLong(2,Long.parseLong(qq));
            ps2.setLong(2,Long.parseLong(qq));
            doCommand(ps);
            doCommand(ps2);
            ps.close();
            ps2.close();
            connection.close();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static BigDecimal get_personal_total_sponsor(String qq){
        BigDecimal sponsor;
        try {
            enableMySQL();
            String s = SQLCommand.GET_SPONSER.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                sponsor = rs.getBigDecimal("Sponsor_total");
                rs.close();
                ps.close();
                connection.close();
                return sponsor;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return null;
    }
    public static BigDecimal get_personal_this_sponsor(String qq){
        BigDecimal sponsor;
        try {
            enableMySQL();
            String s = SQLCommand.GET_THIS_SPONSER.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                sponsor = rs.getBigDecimal("Sponsor_this");
                rs.close();
                ps.close();
                connection.close();
                if (sponsor==null) return BigDecimal.valueOf(0);
                return sponsor;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return null;
    }
    public static BigDecimal get_return_sponsor(String qq){
        BigDecimal sponsor;
        try {
            enableMySQL();
            String s = SQLCommand.GET_RETURN_SPONSER.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                sponsor = rs.getBigDecimal("Sponsor_return");
                rs.close();
                ps.close();
                connection.close();
                return sponsor;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return null;
    }
    public static int set_leave_true(String qq){
        try {
            enableMySQL();
            String s = SQLCommand.SET_LEAVE_ALREADY.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
           doCommand(ps);
           ps.close();
           connection.close();
           return -1;
        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return 0;
    }
    public static byte is_svipp(String qq) {
        byte is_svipp;
        try {
            enableMySQL();
            String s = SQLCommand.GET_IS_SVIPP.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                is_svipp = rs.getByte("is_svipp");
                rs.close();
                ps.close();
                connection.close();
                return is_svipp;
            }
        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return 0;
    }
    public static byte is_ssvip(String qq) {
        byte is_ssvip;
        try {
            enableMySQL();
            String s = SQLCommand.GET_IS_SSVIP.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                is_ssvip = rs.getByte("is_ssvip");
                rs.close();
                ps.close();
                connection.close();
                return is_ssvip;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return 0;
    }
    public static Date get_start_date(String qq) {
        Date start_date;
        try {
            enableMySQL();
            String s = SQLCommand.GET_START_TIME.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setLong(1, Long.parseLong(qq));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                start_date = rs.getDate("start_date");
                rs.close();
                ps.close();
                connection.close();
                return start_date;
            }

        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return null;
    }
    public static int set_start_date(String qq, Date date) {
        try {
            enableMySQL();
            String s = SQLCommand.SET_START_DATE.commandToString();
            PreparedStatement ps = connection.prepareStatement(s);
            ps.setDate(1, date);
            ps.setLong(2, Long.parseLong(qq));
            doCommand(ps);
            ps.close();
            connection.close();
            return -1;
            } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return 0;
    }
}
