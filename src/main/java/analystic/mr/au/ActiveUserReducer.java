package analystic.mr.au;


import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import common.KpiType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName NewUserReducer
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 活跃用户的reducer类
 **/
public class ActiveUserReducer extends Reducer<StatsUserDimension,TimeOutputValue,
        StatsUserDimension,TextOutputValue> {

    private static final Logger logger = Logger.getLogger(ActiveUserReducer.class);
    private StatsUserDimension k = new StatsUserDimension();
    private TextOutputValue v = new TextOutputValue();
    private Set<String> unique = new HashSet<String>();  //用于uuid的去重统计

    private Map<Integer,Set<String>> hourlyMap = new HashMap<>();
    private MapWritable hourlyWritable = new MapWritable();


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //初始化按小时的容器
        for(int i =0 ;i<24;i++){
            this.hourlyMap.put(i,new HashSet<String>() );
            this.hourlyWritable.put(new IntWritable(i),new IntWritable(0) );
        }
    }

    @Override
    protected void reduce(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        if (){

            //构建输出
            this.v.setKpi(KpiType.HOURLY_ACTIVE_USER);
            for (Map.Entry<Integer,Set<String>> en : this.hourlyMap.entrySet()){
                this.hourlyWritable.put(new IntWritable(en.getKey()),new IntWritable())
            }
        }else {
            //清空set
            this.unique.clear();
            //循环
            for (TimeOutputValue tv: values){
                this.unique.add(tv.getId());//循环将uuid添加set中
            }

            //构造输出value
            MapWritable map = new MapWritable();
            map.put(new IntWritable(-1),new IntWritable(this.unique.size()));
            this.v.setValue(map);
            //设置kpi
            this.v.setKpi(KpiType.valueOfKpiName(key.getStatsCommonDimension().getKpiDimension().getKpi_name()));
            //输出
            context.write(key,this.v);
        }
    }
}