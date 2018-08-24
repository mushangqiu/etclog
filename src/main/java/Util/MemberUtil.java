package Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemberUtil {

    private static Map<String, Boolean> cache = new LinkedHashMap<String, Boolean>() {

        /**
         * 缓存大于多少个就去掉
         * @param eldest
         * @return
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return this.size() > 5000;
        }
    };

    /**
     * 判断是否为新增会员
     *
     * @param conn
     * @param memberId
     * @return 新会员true 老会员false
     */
    public static boolean isNewMember(Connection conn, String memberId) {
        //先去缓存查询 info 如果有就是老会员
        Boolean res = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (!cache.containsKey(memberId)) {
                ps = conn.prepareStatement("select `member_id` from `member_info` where member_id = ? ");
                ps.setString(1, memberId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    res = Boolean.valueOf(false);
                } else {
                    res = Boolean.valueOf(true);
                }
                cache.put(memberId, res);
            }else {
                res = Boolean.valueOf(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.close(null, rs, ps);
        }
        return res;
    }

    ;

    public static void deletMemberInfoBvDate(String date, Connection conn) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("delete from `member_info` where created = ? ");
            ps.setString(1, date);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.close(null, null, ps);
        }
    }
}
