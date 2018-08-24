package analystic.mr.nu;

import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import common.KpiType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 新增用户的reducer类
 */
public class NewUserReducer extends Reducer<StatsUserDimension,TimeOutputValue,
        StatsUserDimension,TextOutputValue> {

    private static final Logger logger = Logger.getLogger(NewUserReducer.class);
    private StatsUserDimension k = new StatsUserDimension();
    private TextOutputValue v = new TextOutputValue();
    private Set<String> unique = new HashSet<String>();//用于uuid的去重统计

    @Override
    protected void reduce(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        //清空set
        this.unique.clear();
        for (TimeOutputValue tv:values){
            this.unique.add(tv.getId());//循环将uuid添加到set中
        }
        //构造输出value
        MapWritable map = new MapWritable();
        map.put(new IntWritable(-1), new IntWritable(this.unique.size()));
       this.v.setValue(map);
       //设置kpi
        if(key.getStatsCommonDimension().getKpiDimension().getKpi_name().equals(KpiType.NEW_USER.kpiName)){
            this.v.setKpi(KpiType.NEW_USER);
        }else if(key.getStatsCommonDimension().getKpiDimension().getKpi_name().equals(KpiType.BROWSER_NEW_USER.kpiName)){
            this.v.setKpi(KpiType.BROWSER_NEW_USER);
        }
        context.write(key, this.v);

    }
}
