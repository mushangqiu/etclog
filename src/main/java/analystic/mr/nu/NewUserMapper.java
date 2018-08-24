package analystic.mr.nu;

import analystic.model.dim.StatsCommonDimension;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.base.BrowerDimension;
import analystic.model.dim.base.DateDimension;
import analystic.model.dim.base.KpiDimension;
import analystic.model.dim.base.PlatDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import common.DateEnum;
import common.EventLogConstants;
import common.KpiType;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.List;


/**
 * 统计新增的用户 lauch事件中uuid的去重个数
 */
public class NewUserMapper extends TableMapper<StatsUserDimension,TimeOutputValue> {

        private static final Logger logger = Logger.getLogger(NewUserMapper.class);

        private byte[] famliy = Bytes.toBytes(EventLogConstants.HBASE_COLUMN_FAMILY);
        private StatsUserDimension k =new StatsUserDimension();
        private TimeOutputValue v = new TimeOutputValue();
        private KpiDimension newUserKpi = new KpiDimension(KpiType.NEW_USER.kpiName);
        private KpiDimension browserNewUserKpi = new KpiDimension(KpiType.BROWSER_NEW_USER.kpiName);

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        //从hbase中读取数据
        String serverTime = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME)));
        String uuid = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_UUID)));
        String platfromname = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_PLATFORM)));
        String browserName = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME)));
        String browserVersion = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION)));

        if(StringUtils.isEmpty(uuid)||StringUtils.isEmpty(serverTime)){
            logger.warn("uuid ："+uuid+"serverTime:"+serverTime);
        }
        //构建输出的value
        long lognOfServerTime = Long.valueOf(serverTime);
        this.v.setId(uuid);
        this.v.setTime(lognOfServerTime);
        //构建输出的key
        StatsCommonDimension statsCommonDimension = this.k.getStatsCommonDimension();
        BrowerDimension defaultBrowserDimension = new BrowerDimension("","");

        DateDimension dateDimension = DateDimension.buildDate(lognOfServerTime,DateEnum.DAY);
        List<PlatDimension> platDimension = PlatDimension.bulidList(platfromname);
        List<BrowerDimension> browerDimensions = BrowerDimension.buildList(browserName,browserVersion );

        statsCommonDimension.setDateDimension(dateDimension);
        //循环平台维度输出
        for(PlatDimension pl:platDimension){
            statsCommonDimension.setKpiDimension(newUserKpi);
            statsCommonDimension.setPlatDimension(pl);
            this.k.setBrowerDimension(defaultBrowserDimension);
            this.k.setStatsCommonDimension(statsCommonDimension);
            //输出
            context.write(this.k, this.v);

            //循环浏览器维度集合
            for (BrowerDimension br : browerDimensions){
                this.k.setBrowerDimension(br);
                statsCommonDimension.setKpiDimension(browserNewUserKpi);
                this.k.setStatsCommonDimension(statsCommonDimension);
                //输出   用于浏览器模块下的新增用户的计算
                context.write(this.k,this.v);
            }
        }
    }

}
