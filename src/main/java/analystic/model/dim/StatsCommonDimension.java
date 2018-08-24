package analystic.model.dim;

import analystic.model.dim.base.BaseDimension;
import analystic.model.dim.base.DateDimension;
import analystic.model.dim.base.KpiDimension;
import analystic.model.dim.base.PlatDimension;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * map阶段和reduce阶段输出key公共维度类型的封装
 */
public class StatsCommonDimension extends BaseStatsDimension{
    private DateDimension dateDimension = new DateDimension();
    private PlatDimension platDimension = new PlatDimension();
    private KpiDimension kpiDimension =new KpiDimension();

    public StatsCommonDimension(DateDimension dateDimension, PlatDimension platDimension, KpiDimension kpiDimension) {
        this.dateDimension = dateDimension;
        this.platDimension = platDimension;
        this.kpiDimension = kpiDimension;
    }

    public StatsCommonDimension() {

    }

    /**
     * 克隆当前实例
     * @param dimension
     * @return
     */
    public static StatsCommonDimension clone(StatsCommonDimension dimension){
        PlatDimension platDimension = new PlatDimension(dimension.platDimension.getPlatform_name());
        KpiDimension kpiDimension = new KpiDimension(dimension.kpiDimension.getKpi_name());
        DateDimension dateDimension = new DateDimension(
                dimension.dateDimension.getYear(),dimension.dateDimension.getSeason(),
                dimension.dateDimension.getMonth(),dimension.dateDimension.getWeek(),
                dimension.dateDimension.getDay(),dimension.dateDimension.getType(),
                dimension.dateDimension.getCalendar());
        return new StatsCommonDimension(dateDimension,platDimension,kpiDimension);
    }

    @Override
    public int compareTo(BaseDimension o) {
        if(o == this){
            return 0;
        }
        StatsCommonDimension other = (StatsCommonDimension) o;
        int tmp = this.dateDimension.compareTo(other.dateDimension);
        if(tmp != 0){
            return tmp;
        }
        tmp = this.platDimension.compareTo(other.platDimension);
        if(tmp != 0){
            return tmp;
        }
        return this.kpiDimension.compareTo(other.kpiDimension);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.dateDimension.write(dataOutput);
        this.platDimension.write(dataOutput);
        this.kpiDimension.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.dateDimension .readFields(dataInput);
        this.platDimension .readFields(dataInput);
        this.kpiDimension .readFields(dataInput);
    }

    public DateDimension getDateDimension() {
        return dateDimension;
    }

    public void setDateDimension(DateDimension dateDimension) {
        this.dateDimension = dateDimension;
    }

    public PlatDimension getPlatDimension() {
        return platDimension;
    }

    public void setPlatDimension(PlatDimension platDimension) {
        this.platDimension = platDimension;
    }

    public KpiDimension getKpiDimension() {
        return kpiDimension;
    }

    public void setKpiDimension(KpiDimension kpiDimension) {
        this.kpiDimension = kpiDimension;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateDimension, platDimension, kpiDimension);
    }
}
