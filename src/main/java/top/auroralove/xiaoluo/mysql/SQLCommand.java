package top.auroralove.xiaoluo.mysql;

public enum SQLCommand {
    CREATE_TABLE1(
            "CREATE TABLE IF NOT EXISTS `xiaoluo`(" +
                    "`number` INT UNSIGNED AUTO_INCREMENT," +
                    "`user_qq` BIGINT(20) NOT NULL," +
                    "`user_playername` VARCHAR(20) NOT NULL," +
                    "PRIMARY KEY (`number`))"
    ),
    //这句话就算如果不存在xiaoluo的表，创建之
    //有以下列数
    //number列，存储序号
    //user_qq列，存储QQ，长整数型
    //user_playername列，存储id，字符型
    //主键为number
    ADD_DATA(
            "INSERT INTO xiaoluo " +
                    "(user_qq, user_playername)" +
                    "VALUES (?, ?)"
    ),
    //添加一行数据，包含2个值
    DELETE_DATA_BY_QQ(
            "DELETE FROM xiaoluo WHERE user_qq = ?"
    ),
    //删除qq为[int]的一行数据
    DELETE_DATA_BY_NAME(
            "DELETE FROM xiaoluo WHERE user_playername = ?"
    ),
    SELECT_DATA_BY_QQ(
            "SELECT user_playername FROM xiaoluo WHERE user_qq = ? "+
                    "LIMIT 1"
    ),
    SELECT_DATA_BY_NAME(
            "SELECT user_qq FROM xiaoluo WHERE user_playername = ? "+
                    "LIMIT 1"
    ),

    DELETE_NUMBER(
            "alter table xiaoluo drop number"
    ),
    ADD_NUMBER(
           "alter TABLE xiaoluo add number int(10) primary key auto_increment FIRST"
    ),
    ADD_SPONSER_THIS_IN_TOTAL(
            "SELECT SUM(Sponsor_this) FROM xiaoluo"
    ),
    ADD_PERSONAL_TOTAL_SPONSER_TO_QQ(
            "UPDATE xiaoluo SET Sponsor_total= ? WHERE user_qq = ?"
    ),
    ADD_PERSONAL_THIS_SPONSER_TO_QQ(
            "UPDATE xiaoluo SET Sponsor_this= ? WHERE user_qq = ?"
    ),
    GET_SPONSER(
            "SELECT Sponsor_total FROM xiaoluo WHERE user_qq = ? "+
                    "LIMIT 1"
    ), GET_RETURN_SPONSER(
            "SELECT Sponsor_return FROM xiaoluo WHERE user_qq = ? " +
                    "LIMIT 1"
    ),GET_THIS_SPONSER(
            "SELECT Sponsor_this FROM xiaoluo WHERE user_qq = ? " +
                    "LIMIT 1"
    ),SET_LEAVE_ALREADY(
            "UPDATE xiaoluo SET leave= 1 WHERE user_qq = ?"
    ), GET_IS_SVIPP(
            "SELECT is_svipp FROM xiaoluo WHERE user_qq = ? " +
                    "LIMIT 1"
    ),GET_IS_SSVIP(
            "SELECT is_ssvip FROM xiaoluo WHERE user_qq = ? " +
                    "LIMIT 1"
    ),GET_START_TIME(
            "SELECT start_date FROM xiaoluo WHERE user_qq = ? " +
                    "LIMIT 1"
    ),UPDATE_NAME_BY_QQ(
            "UPDATE xiaoluo SET user_playername = ? WHERE user_qq = ?"
    ),UPDATE_QQ_BY_NAME(
            "UPDATE xiaoluo SET user_qq = ? WHERE user_playername = ?"
    ),SET_START_DATE(
            "UPDATE xiaoluo SET start_date= ? WHERE user_qq = ?"
    );


    private String command;

    SQLCommand(String command)
    {
        this.command = command;
    }
    public String commandToString()
    {
        return command;
    }
}
