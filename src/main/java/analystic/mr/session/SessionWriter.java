package analystic.mr.session;

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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @ClassName NewUserWriter
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description session的ps的赋值
 **/
public class SessionWriter implements IOutputWritter {
    @Override
    public void writter(Configuration conf, BaseStatsDimension key, BaseOutputValueWritable value, PreparedStatement ps,
                        IDimensionConvert convert) throws IOException,SQLException {

        StatsUserDimension statsUserDimension  = (StatsUserDimension) key;
        TextOutputValue textOutputValue = (TextOutputValue) value;
        int sessions = ((IntWritable)textOutputValue.getValue().get(new IntWritable(-1))).get();
        int sessions_length = ((IntWritable)textOutputValue.getValue().get(new IntWritable(-2))).get();

        //为ps赋值
        int i = 0;
        ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getDateDimension()));
        ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getPlatDimension()));
       if(textOutputValue.getKpi().equals(KpiType.BROWSER_SESSION)){
           ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getBrowerDimension()));
       }
        ps.setInt(++i,sessions);
        ps.setInt(++i,sessions_length);
        ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
        ps.setInt(++i,sessions);
        ps.setInt(++i,sessions_length);

        //添加到批处理中
        ps.addBatch();
    }
}