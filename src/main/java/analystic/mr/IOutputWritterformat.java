package analystic.mr;

import Util.JdbcUtil;
import analystic.model.dim.BaseStatsDimension;
import analystic.model.dim.value.BaseOutputValueWritable;
import analystic.mr.service.IDimensionConvert;
import analystic.mr.service.impl.IDimensionConvertImpl;
import common.GlobalConstants;
import common.KpiType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义reduce阶段输出格式类
 */
public class IOutputWritterformat extends OutputFormat<BaseStatsDimension, BaseOutputValueWritable> {
    //    DBOutputFormat
//    FileOutputFormat
    public static final Logger logger = Logger.getLogger(IOutputWritterformat.class);

    //类核心
    @Override
    public RecordWriter<BaseStatsDimension,BaseOutputValueWritable> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        Configuration conf = taskAttemptContext.getConfiguration();
        Connection conn = JdbcUtil.getConn();
        IDimensionConvert convert = new IDimensionConvertImpl();
        return new IOutputRecordWritter(conn,conf,convert);
    }


    //检测输出空间
    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {
        //do nothins
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
//        return new FileOutputCommitter(FileOutputFormat.getOutputPath(),taskAttemptContext);
        return new FileOutputCommitter(null,taskAttemptContext);
    }

    /**
     * 封装输出的内部类
     */
    public class IOutputRecordWritter extends RecordWriter<BaseStatsDimension, BaseOutputValueWritable> {

        private Connection conn = null;
        private Configuration conf = null;
        private IDimensionConvert conv = null;
        //定义两个集合 用于缓存
        private Map<KpiType, Integer> bath = new HashMap<KpiType, Integer>();
        private Map<KpiType, PreparedStatement> map = new HashMap<KpiType, PreparedStatement>();

        public IOutputRecordWritter() {
        }

        public IOutputRecordWritter(Connection conn, Configuration conf, IDimensionConvert conv) {
            this.conn = conn;
            this.conf = conf;
            this.conv = conv;
        }

        //核心方法
        @Override
        public void write(BaseStatsDimension key, BaseOutputValueWritable value) throws IOException, InterruptedException {
            if (key == null || value == null) {
                return;
            }
            try {
                //获取kpi
                KpiType kpi = value.getKpi(); //获取功能标签
                PreparedStatement ps = null;
                int counter = 1;
                if (map.containsKey(kpi)) {
                    ps = map.get(kpi);
                    counter = this.bath.get(kpi);
                    counter++;
                } else {
                    ps = conn.prepareStatement(conf.get(kpi.kpiName));
                    map.put(kpi, ps);
                }
                //将count添加到batch中
                this.bath.put(kpi, counter);
                //为ps赋值
                String writterClassName = conf.get(GlobalConstants.WRITTER_PREFIX + kpi.kpiName + "");
                Class<?> classz = Class.forName(writterClassName);
                IOutputWritter writter = (IOutputWritter) classz.newInstance();//将类转换成接口对象
                        writter.writter(conf, key, value, ps, conv);//调用对应的实现类

                //将赋值好的ps达到一个批量就可以批量处理
                if (counter % GlobalConstants.BATH_NUMBER == 0) {
                    ps.executeBatch();//批量执行
                    conn.commit();
                    this.bath.remove(kpi);
                }
                ps.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
                logger.warn("执行失败");
            }
        }

        @Override
        public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
            //循环map并执行ps
            try {
                for (Map.Entry<KpiType, PreparedStatement> en : map.entrySet()) {
                    en.getValue().executeBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("在close时 执行剩余de的ps错误i");
            } finally {
                JdbcUtil.close(conn,null,null);
                for (Map.Entry<KpiType, PreparedStatement> en : map.entrySet()) {
                    JdbcUtil.close(null, null, en.getValue());
                }
            }
        }
    }
}
