package analystic.model.dim.value;

import common.KpiType;
import org.apache.hadoop.io.Writable;

/**
 * map和reduce输出的value类型的顶级父类
 */
public abstract class BaseOutputValueWritable implements Writable {
    public abstract KpiType getKpi();
}
