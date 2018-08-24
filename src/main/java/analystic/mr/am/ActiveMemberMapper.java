package analystic.mr.am;

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
 * @ClassName NewUserMapper
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 活跃会员；所有事件中memberId的去重个数。
 **/
public class ActiveMemberMapper extends TableMapper<StatsUserDimension,TimeOutputValue> {
    //日志
    private static final Logger logger = Logger.getLogger(ActiveMemberMapper.class);
    //hbase列簇
    private byte[] famliy = Bytes.toBytes(EventLogConstants.HBASE_COLUMN_FAMILY);
    private StatsUserDimension k = new StatsUserDimension();
    private TimeOutputValue v = new TimeOutputValue();
    //定义活跃会员的标签
    private KpiDimension activeMemberKpi = new KpiDimension(KpiType.ACTIVE_MEMBER.kpiName);
    //定义活跃会员 浏览器维度的标签
    private KpiDimension browserActiveMemberKpi = new KpiDimension(KpiType.BROWSER_ACTIVE_MEMBER.kpiName);

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        //从hbase 一行行中获取需要的数据
        String serverTime = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME)));
        String memberId = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_MEMBER_ID)));
        String platformName = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_PLATFORM)));
        String browserName = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME)));
        String browserVersion = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION)));

        //过滤uuid 没有的滤掉
        if(StringUtils.isEmpty(memberId) || StringUtils.isEmpty(serverTime)){
            logger.warn("memberId && serverTime must not null.memberId:"+memberId+"  serverTime:"+serverTime);
            return;
        }

        //构造输出的value
        long longOfServerTime = Long.valueOf(serverTime);
        this.v.setId(memberId);
        this.v.setTime(longOfServerTime);

        //构造输出的key
        StatsCommonDimension statsCommonDimension = this.k.getStatsCommonDimension();
        //默认一个all维度
        BrowerDimension defaultBrowserDimension = new BrowerDimension("","");

        //当前时间维度
        DateDimension dateDimension = DateDimension.buildDate(longOfServerTime,DateEnum.DAY);
        //构建平台维度 所用维度+all维度
        List<PlatDimension> platformDimensions = PlatDimension.bulidList(platformName);
        //构建浏览器维度 所用维度+all维度
        List<BrowerDimension> browserDimensions = BrowerDimension.buildList(browserName,browserVersion);
        //赋值时间维度
        statsCommonDimension.setDateDimension(dateDimension);
        //循环平台维度输出  all

        for (PlatDimension pl :platformDimensions){
            statsCommonDimension.setKpiDimension(activeMemberKpi);
            statsCommonDimension.setPlatDimension(pl);
            //设置默认的浏览器维度
            this.k.setStatsCommonDimension(statsCommonDimension);
            this.k.setBrowerDimension(defaultBrowserDimension);
            //输出
            context.write(this.k,this.v);

            //循环浏览器维度集合
            for (BrowerDimension br : browserDimensions){
                this.k.setBrowerDimension(br);
                statsCommonDimension.setKpiDimension(browserActiveMemberKpi);
                this.k.setStatsCommonDimension(statsCommonDimension);
                //输出   用于浏览器模块下的新增用户的计算
                context.write(this.k,this.v);
            }
        }
    }
}