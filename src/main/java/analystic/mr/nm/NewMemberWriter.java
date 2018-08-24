package analystic.mr.nm;

import Util.TimeUtil;
import analystic.model.dim.BaseStatsDimension;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.BaseOutputValueWritable;
import analystic.model.dim.value.reduce.TextOutputValue;
import analystic.mr.IOutputWritter;
import analystic.mr.service.IDimensionConvert;
import common.GlobalConstants;
import common.KpiType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @ClassName NewUserWriter
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 新增会员的ps的赋值
 **/
public class NewMemberWriter implements IOutputWritter {
    @Override
    public void writter(Configuration conf, BaseStatsDimension key, BaseOutputValueWritable value, PreparedStatement ps,
                        IDimensionConvert convert) throws IOException,SQLException {

        StatsUserDimension statsUserDimension  = (StatsUserDimension) key;
        TextOutputValue textOutputValue = (TextOutputValue) value;

        int i = 0;
        switch (textOutputValue.getKpi()){
            case NEW_MEMBER:
            case BROWSER_NEW_MEMBER:
                int newUsers = ((IntWritable)textOutputValue.getValue().get(new IntWritable(-1))).get();
                //为ps赋值
                ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getDateDimension()));
                ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getPlatDimension()));
                if(textOutputValue.getKpi().equals(KpiType.BROWSER_NEW_MEMBER)){
                    ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getBrowerDimension()));
                }
                ps.setInt(++i,newUsers);
                ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
                ps.setInt(++i,newUsers);
                break;
            case MEMBER_INFO:
                Text memberId = (Text)textOutputValue.getValue().get(new IntWritable(-2));
                ps.setString(++i,memberId.toString());
                ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
                ps.setLong(++i,TimeUtil.parseString2Long(conf.get(GlobalConstants.RUNING_DATE)));
                ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
                ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
                break;
            default:
                throw new RuntimeException("找不到kpi");
        }
        //添加到批处理中
        ps.addBatch();
    }
}