package analystic.mr.pv;

import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import common.KpiType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName NewUserReducer
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 新增会员的reducer类
 **/
public class pvReducer extends Reducer<StatsUserDimension,IntWritable,
        StatsUserDimension,IntWritable> {

    private static final Logger logger = Logger.getLogger(pvReducer.class);
    private TextOutputValue v = new TextOutputValue();
    private Set<String> unique = new HashSet<String>();  //用于uuid的去重统计

    @Override
    protected void reduce(StatsUserDimension key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        //输出
        Iterator<IntWritable> iterator = values.iterator();
        int i = 0;
        while (iterator.hasNext()){
            iterator.next();
            i++;
        }
        context.write(key,new IntWritable(i));
    }
}