package analystic.mr.nu;

import analystic.model.dim.BaseStatsDimension;
import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.BaseOutputValueWritable;
import analystic.model.dim.value.reduce.TextOutputValue;
import analystic.mr.IOutputWritter;
import analystic.mr.service.IDimensionConvert;
import common.GlobalConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @ClassName NewBrowserUserWriter
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description //TODO $
 **/
public class NewBrowserUserWriter implements IOutputWritter {

    @Override
    public void writter(Configuration conf, BaseStatsDimension key, BaseOutputValueWritable value, PreparedStatement ps, IDimensionConvert convert) {
        try {
            StatsUserDimension statsUserDimension  = (StatsUserDimension) key;
            TextOutputValue textOutputValue = (TextOutputValue) value;
            int newUsers = ((IntWritable)textOutputValue.getValue().get(new IntWritable(-1))).get();
            //为ps赋值
            int i = 0;
            ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getDateDimension()));
            ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getPlatDimension()));
            ps.setInt(++i,convert.getDimensionByValue(statsUserDimension.getBrowerDimension()));
            ps.setInt(++i,newUsers);
            ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
            ps.setInt(++i,newUsers);

            //添加到批处理中
            ps.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}