package analystic.mr;


import analystic.model.dim.BaseStatsDimension;
import analystic.model.dim.value.BaseOutputValueWritable;
import analystic.mr.service.IDimensionConvert;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 自定义reduce阶段输出的格式类 为每一个指标的sql语句赋值的接口
 */
public interface IOutputWritter  {
    /**
     * 为每一个sql语句赋值
     * @param conf
     * @param key
     * @param value
     * @param ps
     * @param convert
     */
    void writter(Configuration conf, BaseStatsDimension key, BaseOutputValueWritable value,
            PreparedStatement ps, IDimensionConvert convert) throws IOException, SQLException;
}
