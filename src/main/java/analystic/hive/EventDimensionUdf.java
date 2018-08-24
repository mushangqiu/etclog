package analystic.hive;

import analystic.mr.service.IDimensionConvert;
import analystic.mr.service.impl.IDimensionConvertImpl;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.datanucleus.util.StringUtils;

public class EventDimensionUdf extends UDF {

    private IDimensionConvert convert = new IDimensionConvertImpl();

    public int evaluate(String category,Integer aa){
        if(StringUtils.isEmpty(category)){
        }
        return 1;
    }

}
