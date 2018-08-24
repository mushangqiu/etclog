package analystic.mr.session;

import analystic.model.dim.StatsUserDimension;
import analystic.model.dim.value.map.TimeOutputValue;
import analystic.model.dim.value.reduce.TextOutputValue;
import common.KpiType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * @ClassName NewUserReducer
 * @Author lyd
 * @Date $ $
 * @Vesion 1.0
 * @Description session的reducer类
 **/
public class SessionReducer extends Reducer<StatsUserDimension,TimeOutputValue,
        StatsUserDimension,TextOutputValue> {

    private static final Logger logger = Logger.getLogger(SessionReducer.class);
    private StatsUserDimension k = new StatsUserDimension();
    private TextOutputValue v = new TextOutputValue();
    private Map<String,List<Long>> map = new HashMap<String,List<Long>>();

    @Override
    protected void reduce(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {

        this.map.clear();
        //循环
        for (TimeOutputValue tv: values){
            if(this.map.containsKey(tv.getId())){
                this.map.get(tv.getId()).add(tv.getTime());
            } else {
                List<Long> li = new ArrayList<Long>();
                li.add(tv.getTime());
                this.map.put(tv.getId(),li);
            }
        }
        /**
         * 2018-08-17 all 123 list(123123,123125,123128)
         * 2018-08-17 all 125 list(123111,123112,123222)
         *
         */

        //循环获取时长
        int sessionLength = 0;
        for (Map.Entry<String,List<Long>> en:map.entrySet()){
//            if(en.getValue().size() > 1){
                List<Long> ll = en.getValue();
                sessionLength += Collections.max(ll) - Collections.min(ll);
//            }
        }

        //构造输出value
        MapWritable map = new MapWritable();
        map.put(new IntWritable(-1),new IntWritable(this.map.size()));
        map.put(new IntWritable(-2),new IntWritable(sessionLength % 1000 == 0?sessionLength:sessionLength/1000+1));
        this.v.setValue(map);
        //设置kpi
       this.v.setKpi(KpiType.valueOfKpiName(key.getStatsCommonDimension().getKpiDimension().getKpi_name()));
        //输出
        context.write(key,this.v);
    }
}