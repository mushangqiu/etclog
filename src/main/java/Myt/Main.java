package Myt;

import etl.util.Ip.IPSeeker;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> li = IPSeeker.getInstance().getAllIp();
        int i = 0;
        for (String ip : li) {
            i++;
            String str = new Httpclientpost().doGet("http://ip.taobao.com/service/getIpInfo2.php?ip="+ip);
            System.out.println(new IpJsonUtil().jsonStr2Map(str));
            if (i>10)
                break;
        }
    }
}
