package etl.mr.toHbase;

import common.EventLogConstants;
import etl.util.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * 将hdfs中收集的数据清洗后存储到hbase中   //枚举 其实是指一组固定的常量组成合法值的类型。
 */
public class ToHbaseMapper extends Mapper<Object, Text, NullWritable, Put> {

    private static final Logger logger = Logger.getLogger(ToHbaseMapper.class);
    private int inputRecords, outputRecords, filterRecords = 0;
    private final byte[] family = Bytes.toBytes(EventLogConstants.HBASE_COLUMN_FAMILY);

    private CRC32 crc  = new CRC32(); //对原数据进行检验 判读是否发生数据损坏
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        this.inputRecords++;
        String log = value.toString();
        if (StringUtils.isEmpty(log.trim())) {
            this.filterRecords++;
            return;
        }
        //正常调用日志的工具方法解析
        Map<String, String> info = LogUtil.handleLog(log);
        //根据事件来存储数据
        String eventName = info.get(EventLogConstants.EVENT_COLUMN_NAME_EVENT_NAME);
        EventLogConstants.EventEnum event = EventLogConstants.EventEnum.valueOfAlias(eventName);
        switch (event) {
            case LANUCH:
            case PAGEVVIEW:
            case CHARGEREFUND:
            case CHARGESUCCESS:
            case CHARGEREQUEST:
            case EVENT:
                handleLogToHbase(info, eventName, context);
                break;
            default:
                logger.warn("事件暂时不支持数据的清洗");
                this.filterRecords++;
                break;
        }
    }

    /**
     * 将每一行数据写出
     *
     * @param info
     * @param context
     * @param eventName
     */
    private void handleLogToHbase(Map<String, String> info, String eventName, Context context) {
        if (!info.isEmpty()) {
            //获取构造row-key的字段
            String servertime = info.get(EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME);
            String uuid = info.get(EventLogConstants.EVENT_COLUMN_NAME_UUID);
            String umid = info.get(EventLogConstants.EVENT_COLUMN_NAME_MEMBER_ID);
            try {
                if (StringUtils.isNotEmpty(servertime)) {
                    //构建row-key
                    String rowKey = buildRowKey(servertime, uuid, umid, eventName);
                    Put put = new Put(Bytes.toBytes(rowKey));
                    //循环info 将所有的k-v存储到rok-key行中
                    for (Map.Entry<String, String> en : info.entrySet()) {
                        if(StringUtils.isNotEmpty(en.getKey())){
                            //将kv添加到put中
                            put.addColumn(family, Bytes.toBytes(en.getKey()), Bytes.toBytes(en.getValue()));
                        }
                    }
                    context.write(NullWritable.get(), put);
                    this.outputRecords++;
                } else {
                    this.filterRecords++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构建row-key
     * @param servertime
     * @param uuid
     * @param umid
     * @param eventName
     * @return
     */
    private String buildRowKey(String servertime, String uuid, String umid, String eventName) {
        StringBuffer sb = new StringBuffer(servertime+"_");
        if(StringUtils.isNotEmpty(servertime)){
            this.crc.reset();
            if(StringUtils.isNotEmpty(uuid)){
                this.crc.update(uuid.getBytes());
            }
            if(StringUtils.isNotEmpty(umid)){
                this.crc.update(umid.getBytes());
            }
            if(StringUtils.isNotEmpty(eventName)){
                this.crc.update(eventName.getBytes());
            }
            sb.append(this.crc.getValue() % 1000000000);
        }
        return sb.toString();

    }


    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        logger.info("++++inputRecords:"+inputRecords+"filterRecords:"+filterRecords);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
