package analystic.mr.nu;

import Util.JdbcUtil;
import Util.TimeUtil;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.base.DateDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import analystic.mr.IOutputWritterformat;
import analystic.mr.service.IDimensionConvert;
import analystic.mr.service.impl.IDimensionConvertImpl;
import com.google.common.collect.Lists;
import common.DateEnum;
import common.EventLogConstants;
import common.GlobalConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewUserRunner implements Tool {

    private static final Logger logger = Logger.getLogger(NewUserRunner.class);
    private Configuration conf = new Configuration();

    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(),new NewUserRunner(),args );
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("新增用户运行失败");
        }
    }

    @Override
    public void setConf(Configuration configuration) {
        this.conf.addResource("output-mapping.xml");
        this.conf.addResource("writer-mapping.xml");
        this.conf.addResource("total-mapping.xml");
//        this.conf = HBaseConfiguration.create(configuration);
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        //处理参数
        this.setArgs(conf,args);
        Job job = Job.getInstance(conf,"new user");
        job.setJarByClass(NewUserRunner.class);

        //设置mapper相关属性!
        TableMapReduceUtil.initTableMapperJob(
                this.buildList(job),NewUserMapper.class,
                StatsUserDimension.class, TextOutputValue.class ,
                job,false );

        job.setMapOutputKeyClass(StatsUserDimension.class);
        job.setMapOutputValueClass(TimeOutputValue.class);
        //设置reducer类
        job.setReducerClass(NewUserReducer.class);
        job.setOutputKeyClass(StatsUserDimension.class);
        job.setOutputValueClass(TextOutputValue.class);
        //设置输出的类
        job.setOutputFormatClass(IOutputWritterformat.class);


//        return job.waitForCompletion(true)?0:1;

        /**
         * 运行當天的新增用户，在获取运行日期前一天的新增总用户 相加
         *
         */
        if(job.waitForCompletion(true)){
            this.computeTotalNewUser(job);  //计算新增总用户
            return 0;
        } else {
            return 1;
        }
    }

    private void computeTotalNewUser(Job job) {
        /**
         * 获取运行当天的新增用户，再获取运行日期前一天的新增总用户，然后将相同维度的新增用户和总用户相加即可。
         */
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //获取运行当天的日期
            long nowDate = TimeUtil.parseString2Long(job.getConfiguration().get(GlobalConstants.RUNING_DATE));
            long yesterdayDate = nowDate - GlobalConstants.DAY_OF_MILLSECONDS;

            //构建时间维度对象
            DateDimension nowDateDimension = DateDimension.buildDate(nowDate,DateEnum.DAY);
            DateDimension yesterdayDimension = DateDimension.buildDate(yesterdayDate,DateEnum.DAY);

            //获取对应时间维度的id
            int nowDimensionId = -1;
            int yesterdayDiemnsionId = -1;
            IDimensionConvert convert = new IDimensionConvertImpl();
            nowDimensionId = convert.getDimensionByValue(nowDateDimension);
            yesterdayDiemnsionId = convert.getDimensionByValue(yesterdayDimension);


            //===========================用户模块下==================================
            //获取昨天的新增总用户
            conn = JdbcUtil.getConn();
            Map<String,Integer> map = new HashMap<String,Integer>();
            if(yesterdayDiemnsionId > 0){
                ps = conn.prepareStatement(conf.get(GlobalConstants.TOTAL_PREFIX+"new_total_user"));
                //赋值
                ps.setInt(1,yesterdayDiemnsionId);
                rs = ps.executeQuery();
                while (rs.next()){
                    int platformId = rs.getInt("platform_dimension_id");
                    int totalUser = rs.getInt("total_install_users");
                    //存储
                    map.put(platformId+"",totalUser);
                }
            }

            //获取今天的新增用户
            if(nowDimensionId > 0){
                ps = conn.prepareStatement(conf.get(GlobalConstants.TOTAL_PREFIX+"new_user"));
                //赋值
                ps.setInt(1,nowDimensionId);
                rs = ps.executeQuery();
                while (rs.next()){
                    int platformId = rs.getInt("platform_dimension_id");
                    int newUsers = rs.getInt("new_install_users");
                    //存储
                    if(map.containsKey(platformId+"")){
                        newUsers += map.get(platformId+"");
                    }
                    //覆盖
                    map.put(platformId+"",newUsers);
                }
            }

            //将map中数据进行更新
            ps = conn.prepareStatement(conf.get(GlobalConstants.TOTAL_PREFIX+"new_update_user"));
            for (Map.Entry<String,Integer> en:map.entrySet()){
                //赋值
                ps.setInt(1,nowDimensionId);
                ps.setInt(2,Integer.parseInt(en.getKey()));
                ps.setInt(3,en.getValue());
                ps.setString(4,conf.get(GlobalConstants.RUNING_DATE));
                ps.setInt(5,en.getValue());
                //执行
                ps.addBatch();
            }
            ps.executeBatch();  //批量执行
            //=========================用户模块下end============================================
        } catch (Exception e) {
            logger.warn("计算新增总用户失败.",e);
        } finally {
            JdbcUtil.close(conn,rs,ps);
        }
    }


    private ArrayList<Scan> buildList(Job job) {
        Configuration conf = job.getConfiguration();
        Long startDate = TimeUtil.parseString2Long(conf.get(GlobalConstants.RUNING_DATE));
        long endDate = startDate + GlobalConstants.DAY_OF_MILLSECONDS;
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startDate+""));
        scan.setStopRow(Bytes.toBytes(endDate+""));

        //过滤
        FilterList fl = new FilterList();
        fl.addFilter(new SingleColumnValueFilter(Bytes.toBytes(EventLogConstants.HBASE_TABLE_NAME)
                ,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_EVENT_NAME) ,
                CompareFilter.CompareOp.EQUAL,Bytes.toBytes(EventLogConstants.EventEnum.LANUCH.alias) ));
       //扫描哪些字段
       String[] column ={
               EventLogConstants.EVENT_COLUMN_NAME_UUID,
               EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME,
               EventLogConstants.EVENT_COLUMN_NAME_PLATFORM,
               EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME,
               EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION,
               EventLogConstants.EVENT_COLUMN_NAME_EVENT_NAME
       };
       //将扫描的字符串字段添加到过滤器中
        fl.addFilter(this.getColumFilter(column));

       //设置hbase 表名
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME,Bytes.toBytes(EventLogConstants.HBASE_TABLE_NAME) );
        //将过滤器链添加到scan中

        scan.setFilter(fl);
        return Lists.newArrayList(scan);
    }

    private Filter getColumFilter(String[] column) {
        int length = column.length;
        byte[][] bytes = new byte[length][];
        for(int i = 0; i<length;i++){
            bytes[i] = Bytes.toBytes(column[i]);
        }
        return new MultipleColumnPrefixFilter(bytes);
    }

    private void setArgs(Configuration conf, String[] args) {
        String date = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                if (i + 1 <= args.length) {
                    date = args[i + 1];
                }
            }
        }
        //如果时间为空或者是非法，都将默认运行昨天的数据
        if (StringUtils.isEmpty(date) || !TimeUtil.isValidateDate(date)) {
            date = TimeUtil.getYseterday();
        }
        //将时间存储到confi中
        conf.set(GlobalConstants.RUNING_DATE, date);
    }


}
