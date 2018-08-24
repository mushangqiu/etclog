package Myt;

import com.alibaba.fastjson.JSON;
import etl.util.Country;

import java.util.HashMap;
import java.util.Map;

public class IpJsonUtil {

    /**
     * json 去掉前后空格
     * @param jsonstr
     * @return
     */
    public String jsonStrClear(String jsonstr){
        String[] split = jsonstr.split("\\{");
        return "{"+split[2].substring(0, split[2].length()-3);
    }
    /**
     * json json转 country类
     * @param jsonstr
     * @return
     */
    public Country jsonStr2Class(String jsonstr){
        return  JSON.parseObject(this.jsonStrClear(jsonstr), Country.class);
    }

    /**
     * json 格式转 Map
     * @param jsonstr
     * @return
     */
    public Map jsonStr2Map(String jsonstr){
        if (null == jsonstr || jsonstr.equals(null)){
            System.out.println("数据未获取到值");
            return new HashMap<String,String>();
        }
        Country country = this.jsonStr2Class(jsonstr);
        Map<String,String> country2map = new HashMap<String,String>();
        country2map.put("ip", country.getIp());
        country2map.put("area", country.getArea());
        country2map.put("region", country.getRegion());
        return country2map;
    }
}
