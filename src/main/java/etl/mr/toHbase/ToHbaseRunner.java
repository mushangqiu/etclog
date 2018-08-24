package etl.mr.toHbase;

import Util.TimeUtil;
import common.EventLogConstants;
import common.GlobalConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * 将数据存储到hbase的runner类
 */
public class ToHbaseRunner implements Tool {
    private static final Logger logger = Logger.getLogger(ToHbaseRunner.class);
    private Configuration conf = null;

    @Override
    public void setConf(Configuration configuration) {
        this.conf = HBaseConfiguration.create();
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(), new ToHbaseRunner(), args);
        } catch (Exception e) {
            logger.warn("运行清洗数据到hbase异常", e);
            e.printStackTrace();
        }
    }

    //yarn jar .jar package.class -d 2018-08-17
    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        //处理参数
        processArg(args, conf);

        Job job = Job.getInstance(conf, "to hbase");
        job.setJarByClass(ToHbaseRunner.class);
        //判断hbase的表是否存在 不存在创建
        isTableExists(job);

        //mapj阶段
        job.setMapperClass(ToHbaseMapper.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Put.class);

        //reducer 的位置 本地提交  本地运行（本地提交 集群运行 window上提交 最大化的模拟在yarn上运行 便于开发测试debug）
//        TableMapReduceUtil.initTableReducerJob(EventLogConstants.HBASE_TABLE_NAME, null, job);
        TableMapReduceUtil.initTableReducerJob(EventLogConstants.HBASE_TABLE_NAME, null, job, null, null, null, null, true);
        job.setNumReduceTasks(0);

        //将不能识别的资源文件添加分布式的缓存文件

        //设置输入路径
        setInputPath(job, args);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    /**
     * 处理参数
     *
     * @param args
     * @param conf
     */
    private void processArg(String[] args, Configuration conf) {
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
     * 判断hbase的表 预分区？？
     *
     * @param job
     */
    private void isTableExists(Job job) {
        Connection conn = null;
        Admin admin = null;
        try {
            conn = ConnectionFactory.createConnection(job.getConfiguration());
            admin = conn.getAdmin();
            TableName tn = TableName.valueOf(EventLogConstants.HBASE_TABLE_NAME);
            if (!admin.tableExists(tn)) {
                HTableDescriptor hdc = new HTableDescriptor(tn);
                HColumnDescriptor hcd = new HColumnDescriptor(Bytes.toBytes(EventLogConstants.HBASE_COLUMN_FAMILY));
                hdc.addFamily(hcd);
                admin.createTable(hdc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    /**
     * 设置清洗数据的输入路径
     *
     * @param job
     * @param args
     */
    private void setInputPath(Job job, String[] args) {
        try {
            String date = job.getConfiguration().get(GlobalConstants.RUNING_DATE);
            String[] fileds = date.split("-");
            Path inputpath = new Path("/logs/" + fileds[1] + "/" + fileds[2]);
            FileSystem fs = FileSystem.get(job.getConfiguration());
            if (fs.exists(inputpath)){
                FileInputFormat.addInputPath(job,inputpath);
            }else {
                throw new RuntimeException("数据输入路径不存在.input:"+inputpath.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
