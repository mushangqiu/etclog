package analystic.model.dim.value.reduce;

import analystic.model.dim.value.BaseOutputValueWritable;
import common.KpiType;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 用户模块和浏览器
 */
public class TextOutputValue extends BaseOutputValueWritable {
    private KpiType kpi;
    private MapWritable value = new MapWritable();

    public TextOutputValue() {
    }

    public TextOutputValue(KpiType kpi, MapWritable value) {
        this.kpi = kpi;
        this.value = value;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        WritableUtils.writeEnum(dataOutput, kpi);
        this.value.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        WritableUtils.readEnum(dataInput, KpiType.class);
        this.value.readFields(dataInput);
    }

    public KpiType getKpi(){
        return this.kpi;
    }

    public void setKpi(KpiType kpi) {
        this.kpi = kpi;
    }

    public MapWritable getValue() {
        return value;
    }

    public void setValue(MapWritable value) {
        this.value = value;
    }
}
