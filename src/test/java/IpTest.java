import etl.util.IPParserUtil;
import etl.util.Ip.IPSeeker;

import java.util.List;

public class IpTest {
    public static void main(String[] args) {
        System.out.println(new IPParserUtil().parserIP("221.11.112.123"));
        List<String> li = IPSeeker.getInstance().getAllIp();
        for (String str : li ){
            System.out.println(new IPParserUtil().parserIP(str));
        }
    }
}
