package analystic.mr.service.impl;

import Util.JdbcUtil;
import analystic.model.dim.base.*;
import analystic.mr.nu.NewUserMapper;
import analystic.mr.service.IDimensionConvert;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 根据维度获取维度id的接口实现
 */
public class IDimensionConvertImpl implements IDimensionConvert {
    private static final Logger logger = Logger.getLogger(NewUserMapper.class);

    private Map<String, Integer> cache = new LinkedHashMap<String, Integer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return this.size() > 1000;
        }
    };

    /**
     * 获取对应维度的id
     *
     * @param dimension
     * @return
     */
    @Override
    public int getDimensionByValue(BaseDimension dimension) {

        try{
            //生成维度缓存key
            String cacheKey = buildCacheKey(dimension);
            if(this.cache.containsKey(cacheKey)){
                return this.cache.get(cacheKey);
            }
            //代码走到这，代表cache中没有对应的维度
            //去mysql中先查找 如有则返回id 如没有将先插入再返回维度
            Connection conn = JdbcUtil.getConn();
            String[] sqls = null ;
            if(dimension instanceof PlatDimension){
                sqls = buildPlatFromSqls(dimension);
            }else if(dimension instanceof KpiDimension){
                sqls = buildKpiFromSqls(dimension);
            }else if(dimension instanceof BrowerDimension){
                sqls = buildBrowerFromSqls(dimension);
            }else if(dimension instanceof DateDimension){
                sqls = buildDateFromSqls(dimension);
            }else {
                throw new RuntimeException();
            }

            //执行sql
            int id = -1;
            synchronized (this){
                id = this.executeSqls(sqls,dimension,conn);
            }
            //id放到缓存
            this.cache.put(cacheKey,id );
            return id;
        }catch (Exception e){
            logger.warn("获取id异常");
        }
        throw new RuntimeException();
    }

    private int executeSqls(String[] sqls, BaseDimension dimension, Connection conn) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            ps = conn.prepareStatement(sqls[0]);
            this.setArgs(dimension,ps);
            rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("id");
            }
            //代码走到这 代表没有查询到对应的id 则插入并查询
            ps = conn.prepareStatement(sqls[1],Statement.RETURN_GENERATED_KEYS);
            this.setArgs(dimension,ps);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if(rs.next()){
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            logger.warn("执行sql异常");
        }
        throw new RuntimeException("执行sql语句异常");
    }
    //设置参数
    private void setArgs(BaseDimension dimension, PreparedStatement ps) {
        try {
            int i = 0;
            if(dimension instanceof PlatDimension){
                PlatDimension platDimension = (PlatDimension)dimension;
                ps.setString(++i,platDimension.getPlatform_name());
            }else if(dimension instanceof BrowerDimension){
                BrowerDimension browerDimension = (BrowerDimension)dimension;
                ps.setString(++i,browerDimension.getBrowser_name());
                ps.setString(++i,browerDimension.getBrowser_version());
            }else if(dimension instanceof KpiDimension){
                KpiDimension kpiDimension = (KpiDimension)dimension;
                ps.setString(++i,kpiDimension.getKpi_name());
            }else if(dimension instanceof DateDimension){
                DateDimension dateDimension = (DateDimension)dimension;
                ps.setInt(++i,dateDimension.getYear());
                ps.setInt(++i,dateDimension.getSeason());
                ps.setInt(++i,dateDimension.getMonth());
                ps.setInt(++i,dateDimension.getWeek());
                ps.setInt(++i,dateDimension.getDay());
                ps.setString(++i,dateDimension.getType());
                ps.setDate(++i,new Date((dateDimension.getCalendar().getTime())));
            }else {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String[] buildDateFromSqls(BaseDimension dimension) {
        String query = "select id from `dimension_date` where `year` = ? " +
                "and `season` =? and `month` =? and `week` =? and `day` =? " +
                "and `type` =? and `calendar` =?";
        String insert = "insert into `dimension_date` " +
                "(`year`,`season`,`month`,`week`,`day`,`type`,`calendar`) " +
                "value(?,?,?,?,?,?,?) ";
        return new String[]{query,insert} ;
    }

    private String[] buildBrowerFromSqls(BaseDimension dimension) {

        String query = "select id from `dimension_browser` where `browser_name` = ? and `browser_version` = ?";
        String insert = "insert into `dimension_browser` (`browser_name`,`browser_version`) value(?,?) ";
        return new String[]{query,insert} ;
    }

    private String[] buildKpiFromSqls(BaseDimension dimension) {

        String query = "select id from `dimension_kpi` where `kpi_name` = ?";
        String insert = "insert into `dimension_kpi` (`kpi_name`)value(?) ";
        return new String[]{query,insert} ;
    }

    /**
     * 查询id 第二个插入sql
     * @param dimension
     * @return
     */
    private String[] buildPlatFromSqls(BaseDimension dimension) {
        String query = "select id from `dimension_platform` where `platform_name` = ?";
        String insert = "insert into `dimension_platform` (`platform_name`) value(?) ";
        return new String[]{query,insert} ;
    }

    private String buildCacheKey(BaseDimension dimension) {

        StringBuffer sb = new StringBuffer();

        if(dimension instanceof PlatDimension){
            PlatDimension plat = (PlatDimension)dimension;
            sb.append("platform_");
            sb.append(plat.getPlatform_name());
        }else if(dimension instanceof KpiDimension){
            KpiDimension kpi = (KpiDimension)dimension;
            sb.append("kpi_");
            sb.append(kpi.getKpi_name());
        }else if(dimension instanceof BrowerDimension){
            BrowerDimension brower = (BrowerDimension)dimension;
            sb.append("brower_");
            sb.append(brower.getBrowser_name());
            sb.append(brower.getBrowser_version());
        }else if(dimension instanceof DateDimension){
            DateDimension date = (DateDimension)dimension;
            sb.append("date_");
            sb.append(date.getYear());
            sb.append(date.getSeason());
            sb.append(date.getMonth());
            sb.append(date.getWeek());
            sb.append(date.getDay());
            sb.append(date.getType());
        }
        return sb.toString();
    }


}
