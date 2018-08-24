import etl.util.LogUtil;

import java.util.Map;

public class Logtest {
    public static void main(String[] args) {
        Map<String,String> map = LogUtil.handleLog("192.168.216.1^A1499154239.909^A192.168.216.123^A/1603.JPG?en=e_crt&oid=123456&on=%E6%B5%8B%E8%AF%95%E8%AE%A2%E5%8D%95123456&cua=524.01&cut=RMB&pt=alipay&ver=1&pl=website&sdk=js&u_ud=E9670BE2-FCEC-4DA7-AF27-DCF5B4188F32&u_sd=1055AC27-D607-4F31-B4AF-ABEC7B8F6412&c_time=1499154240185&l=zh-CN&b_iev=Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20WOW64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F49.0.2623.221%20Safari%2F537.36%20SE%202.X%20MetaSr%201.0&b_rst=1600*900\n");
        for (Map.Entry<String,String> en: map.entrySet()){
            System.out.println(en.getKey()+en.getValue());
        }
    }
}
