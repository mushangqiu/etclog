package analystic.model.dim;

import analystic.model.dim.base.BaseDimension;
import analystic.model.dim.base.BrowerDimension;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StatsUserDimension extends BaseStatsDimension{

    private BrowerDimension browerDimension = new BrowerDimension();
    private StatsCommonDimension statsCommonDimension=new StatsCommonDimension();

    public StatsUserDimension(BrowerDimension browerDimension, StatsCommonDimension statsCommonDimension) {
        this.browerDimension = browerDimension;
        this.statsCommonDimension = statsCommonDimension;
    }

    public StatsUserDimension() {
    }

    @Override
    public int compareTo(BaseDimension o) {
        if(o == this){
            return 0;
        }
        StatsUserDimension other = (StatsUserDimension) o;
        int tmp = this.browerDimension.compareTo(other.browerDimension);
        if(tmp != 0){
            return tmp;
        }
        return this.statsCommonDimension.compareTo(other.statsCommonDimension);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.browerDimension.write(dataOutput);
        this.statsCommonDimension.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.browerDimension.readFields(dataInput);
        this.statsCommonDimension.readFields(dataInput);
    }

    public static StatsUserDimension clone(StatsUserDimension dimension){
        BrowerDimension browerDimension =new BrowerDimension(dimension.browerDimension.getBrowser_name(),
                dimension.browerDimension.getBrowser_version());
        StatsCommonDimension statsCommonDimension =StatsCommonDimension.clone(dimension.statsCommonDimension);
        return new StatsUserDimension(browerDimension,statsCommonDimension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatsUserDimension)) return false;
        StatsUserDimension that = (StatsUserDimension) o;
        return Objects.equals(browerDimension, that.browerDimension) &&
                Objects.equals(statsCommonDimension, that.statsCommonDimension);
    }

    @Override
    public int hashCode() {

        return Objects.hash(browerDimension, statsCommonDimension);
    }

    public BrowerDimension getBrowerDimension() {
        return browerDimension;
    }

    public void setBrowerDimension(BrowerDimension browerDimension) {
        this.browerDimension = browerDimension;
    }

    public StatsCommonDimension getStatsCommonDimension() {
        return statsCommonDimension;
    }

    public void setStatsCommonDimension(StatsCommonDimension statsCommonDimension) {
        this.statsCommonDimension = statsCommonDimension;
    }
}
