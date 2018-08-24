import Myt.IpJsonUtil;

public class httpclient {
    public static void main(String[] args) throws Exception {

//        String jsonstr = new Httpclientpost().doGet("http://ip.taobao.com/service/getIpInfo2.php?ip=19.23.56.1");
//        System.out.println(jsonstr);
        String jsonstr = "{\"code\":0,\"data\":{\"ip\":\"19.23.56.1\",\"country\":\"美国\",\"area\":\"\",\"region\":\"密歇根\",\"city\":\"XX\",\"county\":\"XX\",\"isp\":\"XX\",\"country_id\":\"US\",\"area_id\":\"\",\"region_id\":\"US_122\",\"city_id\":\"xx\",\"county_id\":\"xx\",\"isp_id\":\"xx\"}}\n" +
                "\n";
//        String[] split = jsonstr.split("\\{");
//        String a = "{"+split[2].substring(0, split[2].length()-3);
//        country op = JSON.parseObject(a, country.class);
//        System.out.println(op);

        new IpJsonUtil().jsonStr2Map(jsonstr);
    }
}
