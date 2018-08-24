package analystic.model.dim.base;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class KpiDimension extends BaseDimension {

    private int id = 0;
    private String kpi_name = ""; //'kpi维度名称'

    public KpiDimension() {

    }

    public KpiDimension(String kpi_name) {
        this.kpi_name = kpi_name;
    }

    public KpiDimension(int id, String kpi_name) {
        this.id = id;
        this.kpi_name = kpi_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKpi_name() {
        return kpi_name;
    }

    public void setKpi_name(String kpi_name) {
        this.kpi_name = kpi_name;
    }


    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.id);
        out.writeUTF(this.kpi_name);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readInt();
        this.kpi_name = in.readUTF();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,kpi_name);
    }


    @Override
    public int compareTo(BaseDimension o) {
        if (o==this){
            return 0;
        }
        KpiDimension other = (KpiDimension) o;
        int tmp = this.id - other.id;
        if(tmp!=0){
            return tmp;
        }
        return this.kpi_name.compareTo(other.kpi_name);
    }

}
