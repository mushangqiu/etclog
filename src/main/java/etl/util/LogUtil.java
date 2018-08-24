package etl.util;

import common.EventLogConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 整行日志的解析工具
 */
public class LogUtil {
    private static final Logger logger = Logger.getLogger(LogUtil.class);

    /**
     * 单行日志的解析
     * @param log
     * @return
     */
    public static Map<String,String> handleLog(String log){
        Map<String, String> info = new ConcurrentHashMap<>();//线程安全 效率高 多用于底层架构
        if(StringUtils.isNotEmpty(log)){
            //拆分单行日志
            String[] fields = log.split(EventLogConstants.LOG_SEPARTOR);
            if (fields.length == 3){
                //存储数据到info
                info.put(EventLogConstants.EVENT_COLUMN_NAME_IP, fields[0]);
                info.put(EventLogConstants.EVENT_COLUMN_NAME_SERVER_TIME, fields[1].replaceAll("\\.","" ));
                //处理参数列表
                handleParams(info,fields[2]);
                handleIp(info);
                //处理useragent
                handleUserAgent(info);
            }
        }
        return info;
    }

    /**
     * 处理agent
     * @param info
     */
    private static void handleUserAgent(Map<String,String> info) {
        if(info.containsKey(EventLogConstants.EVENT_COLUMN_NAME_USERAGENT)){
            UserAgentUtil.UserAgentInfo ua = UserAgentUtil.parseUserAgent(info.get(EventLogConstants.EVENT_COLUMN_NAME_USERAGENT));
            if(ua!=null){
                info.put(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_NAME, ua.getBrowserName());
                info.put(EventLogConstants.EVENT_COLUMN_NAME_BROWSER_VERSION, ua.getBrowerVersion());
                info.put(EventLogConstants.EVENT_COLUMN_NAME_OS_NAME, ua.getOsName());
                info.put(EventLogConstants.EVENT_COLUMN_NAME_OS_VERSION, ua.getOsVersion());
            }
        }
    }

    private static void handleIp(Map<String,String> info) {
        if(info.containsKey(EventLogConstants.EVENT_COLUMN_NAME_IP)){
            IPParserUtil.RegionInfo ua = IPParserUtil.parserIP(info.get(EventLogConstants.EVENT_COLUMN_NAME_IP));
            if(ua!=null){
                info.put(EventLogConstants.EVENT_COLUMN_NAME_COUNTRY, ua.getCountry());
                info.put(EventLogConstants.EVENT_COLUMN_NAME_PROVINCE, ua.getProvince());
                info.put(EventLogConstants.EVENT_COLUMN_NAME_CITY, ua.getCity());
            }
        }
    }

    /**
     * 处理参数
     * @param info
     * @param field
     */
    private static void handleParams(Map<String,String> info, String field) {
        if(StringUtils.isNotEmpty(field)){
            int index = field.indexOf("?");
            if(index>0){
                String fields = field.substring(index+1);
                String[] param = fields.split("&");
                for (int i = 0;i<param.length;i++){
                    String[] kvs = param[i].split("=");
                    try {
                        String k = kvs[0];
                        String v = URLDecoder.decode(kvs[1],"utf-8" );
                        if(StringUtils.isNotEmpty(k)){
                            info.put(k, v);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
