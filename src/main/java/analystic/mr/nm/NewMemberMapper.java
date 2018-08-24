package analystic.mr.nm;

import Util.JdbcUtil;
import Util.MemberUtil;
import analystic.model.dim.StatsCommonDimension;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.base.BrowerDimension;
import analystic.model.dim.base.DateDimension;
import analystic.model.dim.base.KpiDimension;
import analystic.model.dim.base.PlatDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import common.DateEnum;
import common.EventLogConstants;
import common.GlobalConstants;
import common.KpiType;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * @ClassName NewUserMapper
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 新增会员；所有事件中memberId的去重个数。
 **/
public class NewMemberMapper extends TableMapper<StatsUserDimension,TimeOutputValue> {

    private static final Logger logger = Logger.getLogger(NewMemberMapper.class);
    private byte[] famliy = Bytes.toBytes(EventLogConstants.HBASE_COLUMN_FAMILY);
    private StatsUserDimension k = new StatsUserDimension();
    private TimeOutputValue v = new TimeOutputValue();
    private KpiDimension newMemberKpi = new KpiDimension(KpiType.NEW_MEMBER.kpiName);
    private KpiDimension browserNewMemberKpi = new KpiDimension(KpiType.BROWSER_NEW_MEMBER.kpiName);
    private Connection conn = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        conn = JdbcUtil.getConn();
        //删除当天的会员信息
        MemberUtil.deletMemberInfoBvDate(conf.get(GlobalConstants.RUNING_DATE),conn);
    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        //从hbase中读取数据
        String serverTime = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME)));
        String memberId = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_MEMBER_ID)));
        String platformName = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_PLATFORM)));
        String browserName = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME)));
        String browserVersion = Bytes.toString(value.getValue(famliy,Bytes.toBytes(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION)));

        if(StringUtils.isEmpty(memberId) || StringUtils.isEmpty(serverTime)){
            logger.warn("memberId && serverTime must not null.memberId:"+memberId+"  serverTime:"+serverTime);
            return;
        }

        //memberId是否是一个新增的会员
        if(!MemberUtil.isNewMember(conn,memberId)){
            logger.info("该memberId是一个老会员.memberId:"+memberId);
            return;
        }

        //构造输出的value
        long longOfServerTime = Long.valueOf(serverTime);
        this.v.setId(memberId);
        this.v.setTime(longOfServerTime);

        //构造输出的key
        StatsCommonDimension statsCommonDimension = this.k.getStatsCommonDimension();
        BrowerDimension defaultBrowserDimension = new BrowerDimension("","");

        DateDimension dateDimension = DateDimension.buildDate(longOfServerTime,DateEnum.DAY);
        List<PlatDimension> platformDimensions = PlatDimension.bulidList(platformName);
        List<BrowerDimension> browserDimensions = BrowerDimension.buildList(browserName,browserVersion);

        statsCommonDimension.setDateDimension(dateDimension);
        //循环平台维度输出  all

        for (PlatDimension pl :platformDimensions){
            statsCommonDimension.setKpiDimension(newMemberKpi);
            statsCommonDimension.setPlatDimension(pl);
            //设置默认的浏览器维度
            this.k.setStatsCommonDimension(statsCommonDimension);
            this.k.setBrowerDimension(defaultBrowserDimension);
            //输出
            context.write(this.k,this.v);

            //循环浏览器维度集合
            for (BrowerDimension br : browserDimensions){
                this.k.setBrowerDimension(br);
                statsCommonDimension.setKpiDimension(browserNewMemberKpi);
                this.k.setStatsCommonDimension(statsCommonDimension);
                //输出   用于浏览器模块下的新增用户的计算
                context.write(this.k,this.v);
            }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        JdbcUtil.close(conn,null,null);
    }
}