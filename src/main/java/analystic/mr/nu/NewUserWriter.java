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

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NewUserWriter implements IOutputWritter {
    @Override
    public void writter(Configuration conf, BaseStatsDimension key, BaseOutputValueWritable value, PreparedStatement ps, IDimensionConvert convert) {
        StatsUserDimension statsUserDimension = (StatsUserDimension)key;
        TextOutputValue textOutputValue = (TextOutputValue)value;
        int newUsers = ((IntWritable)textOutputValue.getValue().get(new IntWritable(-1))).get();
        int i =0 ;
        try {
            ps.setInt(++i, convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getDateDimension()));
            ps.setInt(++i, convert.getDimensionByValue(statsUserDimension.getStatsCommonDimension().getPlatDimension()));
            ps.setInt(++i,newUsers);
            ps.setString(++i,conf.get(GlobalConstants.RUNING_DATE));
            ps.setInt(++i,newUsers);

            ps.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}