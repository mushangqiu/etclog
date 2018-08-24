import analystic.model.dim.base.PlatDimension;
import analystic.mr.service.IDimensionConvert;
import analystic.mr.service.impl.IDimensionConvertImpl;

public class DimensionTest {
    public static void main(String[] args) {
        PlatDimension pl = new PlatDimension();
        IDimensionConvert convert = new IDimensionConvertImpl();
        System.out.println(convert.getDimensionByValue(pl));
    }
}
