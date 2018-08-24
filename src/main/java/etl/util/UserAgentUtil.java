package etl.util;

import cz.mallat.uasparser.OnlineUpdater;
import cz.mallat.uasparser.UASparser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;


public class UserAgentUtil {

    private static final Logger logger = Logger.getLogger(UserAgentUtil.class);

    private static UASparser ua =null;

    static {
        try {
            ua = new UASparser(OnlineUpdater.getVendoredInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 解析浏览器代理对象
     * @param agent
     * @return
     */
    public static UserAgentInfo parseUserAgent(String agent){
        UserAgentInfo uainfo = new UserAgentInfo();
        if(StringUtils.isEmpty(agent)){
          logger.warn("agent is null.but we need not null.");
            return null;
        }
        try{
            cz.mallat.uasparser.UserAgentInfo info = ua.parse(agent);
            //设置属性
            uainfo.setBrowserName(info.getUaFamily());
            uainfo.setBrowerVersion(info.getBrowserVersionInfo());
            uainfo.setOsName(info.getOsFamily());
            uainfo.setOsVersion(info.getOsName());
        }catch (IOException e) {
            logger.warn("useragent parse 异常.",e);
        }
        return  uainfo;
    }


    /**
     * 封装解析出来的字段 浏览器名 版本 系统名版主
     */
    public static class UserAgentInfo{
        private String browserName;
        private String browerVersion;
        private String osName;
        private String osVersion;

        public UserAgentInfo(){

        }

        public UserAgentInfo(String browserName, String browerVersion, String osName, String osVersion) {
            this.browserName = browserName;
            this.browerVersion = browerVersion;
            this.osName = osName;
            this.osVersion = osVersion;
        }

        public String getBrowserName() {
            return browserName;
        }

        public void setBrowserName(String browserName) {
            this.browserName = browserName;
        }

        public String getBrowerVersion() {
            return browerVersion;
        }

        public void setBrowerVersion(String browerVersion) {
            this.browerVersion = browerVersion;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        @Override
        public String toString() {
            return "UserAgentInfo{" +
                    "browserName='" + browserName + '\'' +
                    ", browerVersion='" + browerVersion + '\'' +
                    ", osName='" + osName + '\'' +
                    ", osVersion='" + osVersion + '\'' +
                    '}';
        }
    }
}
