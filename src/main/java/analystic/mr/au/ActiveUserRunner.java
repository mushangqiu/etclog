package analystic.mr.au;

import Util.TimeUtil;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import analystic.mr.IOutputWritterformat;
import common.EventLogConstants;
import common.GlobalConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName NewUserRunner
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 活跃用户的驱动类
 *
 **/
public class ActiveUserRunner implements Tool {
   private static final Logger logger = Logger.getLogger(ActiveUserRunner.class);
   private Configuration conf =new Configuration();

    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(),new ActiveUserRunner(),args);
        } catch (Exception e) {
            logger.warn("活跃用户运行失败.",e);
        }
    }

    @Override
    public void setConf(Configuration configuration) {
        configuration.addResource("output-mapping.xml");
        configuration.addResource("writer-mapping.xml");
//        this.conf = HBaseConfiguration.create(configuration); //???
        this.conf = configuration;
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
        Job job = Job.getInstance(conf,"active user");
        job.setJarByClass(ActiveUserRunner.class);

        //设置mapper先关属性
        TableMapReduceUtil.initTableMapperJob(this.buildList(job),ActiveUserMapper.class,
                StatsUserDimension.class,TimeOutputValue.class,job,
                true);
        job.setMapOutputKeyClass(StatsUserDimension.class);
        job.setMapOutputValueClass(TimeOutputValue.class);

        //设置reducer类
        job.setReducerClass(ActiveUserReducer.class);
        job.setOutputKeyClass(StatsUserDimension.class);
        job.setOutputValueClass(TextOutputValue.class);

        //设置输出的类
        job.setOutputFormatClass(IOutputWritterformat.class);

        return job.waitForCompletion(true)?0:1;
    }


    /**
     * 处理输入的参数
     * @param conf
     * @param args
     */
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

    /**
     * 获取hbase的扫描对象
     * @param job
     * @return
     */
    private List<Scan> buildList(Job job) {
        Configuration conf = job.getConfiguration();
        Long startDate = TimeUtil.parseString2Long(conf.get(GlobalConstants.RUNING_DATE));
        long endDate = startDate + GlobalConstants.DAY_OF_MILLSECONDS;
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startDate+""));
        scan.setStopRow(Bytes.toBytes(endDate+""));
//        Scan scan1 = new Scan();

        //过滤
        FilterList fl = new FilterList();

        //扫描哪些字段
        String[] columns = {
                EventLogConstants.EVENT_COLUMN_NAME_UUID,
                EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME,
                EventLogConstants.EVENT_COLUMN_NAME_PLATFORM,
                EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME,
                EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION,
                EventLogConstants.EVENT_COLUMN_NAME_EVENT_NAME
        };
        //将扫描的字段添加到过滤器中
        fl.addFilter(this.getColumnFilter(columns));
        //设置hbase表名
        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME,Bytes.toBytes(EventLogConstants.HBASE_TABLE_NAME));
//        scan1.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME,Bytes.toBytes(EventLogConstants.HBASE_TABLE_NAME));

        //将过滤器链添加到scan中
        scan.setFilter(fl);

//        return Lists.newArrayList(scan);  //google中
        List<Scan> li = new ArrayList<Scan>();
//        li.add(scan1);
        li.add(scan);
        return li;
    }

    /**
     * 设置扫描的字段
     * @param columns
     * @return
     */
    private Filter getColumnFilter(String[] columns) {
        int length = columns.length;
        byte[][] bytes = new byte[length][];
        for (int i = 0; i < length;i++){
            bytes[i] = Bytes.toBytes(columns[i]);
        }
        return new MultipleColumnPrefixFilter(bytes);
    }
}