package Util;

import common.GlobalConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 获取和关闭数据库的连接
 */
public class JdbcUtil {

    static {
        try {
            Class.forName(GlobalConstants.DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConn(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(GlobalConstants.URL,GlobalConstants.USERNAME,GlobalConstants.PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭mysql的相关对象
     * @param conn
     * @param rs
     * @param o
     */

  public static void close(Connection conn, ResultSet rs, Object o){
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
  }

    public static void main(String[] args) {
        System.out.println(JdbcUtil.getConn());
    }
}
