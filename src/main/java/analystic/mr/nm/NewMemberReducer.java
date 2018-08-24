package analystic.mr.nm;

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
import java.util.Set;

/**
 * @ClassName NewUserReducer
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description 新增会员的reducer类
 **/
public class NewMemberReducer extends Reducer<StatsUserDimension,TimeOutputValue,
        StatsUserDimension,TextOutputValue> {

    private static final Logger logger = Logger.getLogger(NewMemberReducer.class);
    private TextOutputValue v = new TextOutputValue();
    private Set<String> unique = new HashSet<String>();  //用于uuid的去重统计

    @Override
    protected void reduce(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        //清空set
        this.unique.clear();
        //循环
        for (TimeOutputValue tv: values){
            this.unique.add(tv.getId());//循环将memberId添加set中
        }

        //构造输出value
        MapWritable map = new MapWritable();
        //新增会员Id存储到mysql
        for (String memberId:this.unique){
            map.put(new IntWritable(-2),new Text(memberId));
            this.v.setValue(map);
            this.v.setKpi(KpiType.MEMBER_INFO);
            //写出  专门用于存储新增会员
            context.write(key,this.v);
        }

        map.put(new IntWritable(-1),new IntWritable(this.unique.size()));
        this.v.setValue(map);
        //设置kpi
       this.v.setKpi(KpiType.valueOfKpiName(key.getStatsCommonDimension().getKpiDimension().getKpi_name()));
        //输出
        context.write(key,this.v);
    }
}