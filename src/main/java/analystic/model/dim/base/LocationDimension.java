package analystic.model.dim.base;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LocationDimension extends BaseDimension{

    private int id

    @Override
    public int compareTo(BaseDimension o) {
        return 0;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {

    }
}
