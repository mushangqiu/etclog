package Util;

import common.DateEnum;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局的事件工具类
 */
public class TimeUtil {
    private static final String DEFAULT_FORMAT="yyyy-MM-dd";

    /**
     * 判断事件是否有效  正则表达式 yyyy-MM-dd
     * @param date
     * @return
     */
    public static boolean isValidateDate(String date){
        Matcher matcher = null;
        Boolean res = false;
        String regexp = "^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}";
        if (StringUtils.isNotEmpty(date)){
            Pattern pattern = Pattern.compile(regexp);
            matcher = pattern.matcher(date);
        }
        if (matcher != null){
            res = matcher.matches();
        }
        return res;
    }

    /**
     * 默认获取昨天的日期
     * @return
     */
    public static String getYseterday(){
        return getYseterday(DEFAULT_FORMAT);
    }

    /**
     * 获取指定格式的昨天日期
     * @param pattern
     * @return
     */
    public static String getYseterday(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        return sdf.format(calendar.getTime());
    }

    /**
     * 将时间戳转换成默认格式的日期
     * @param time
     * @return
     */
    public static String parseLong2String(Long time){
        return parseLong2String(time, DEFAULT_FORMAT);
    }

    /**
     * 将时间戳转换成指定格式的日期
     * @param time
     * @param pattern
     * @return
     */
    public static String parseLong2String(Long time,String pattern){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return new SimpleDateFormat(pattern).format(calendar.getTime());
    }
    /**
     * 将时间戳转换成默认格式的日期
     * @param date
     * @return
     */
    public static Long parseString2Long(String date){
        return parseString2SLong(date, DEFAULT_FORMAT);
    }

    /**
     * 将时间戳转换成指定格式的日期
     * @param date
     * @param pattern
     * @return
     */
    public static Long parseString2SLong(String date,String pattern){
        Calendar calendar = Calendar.getInstance();
        Date dt = null;
        try {
            dt = new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt.getTime();
    }

    /**
     * 获取周第一天时间戳
     * @param time
     * @return
     */
    public static long getFirsrDayOfWeek(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_WEEK,1);//该周第一天
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTimeInMillis();
    }

    //获取日期信息
    public static int getDateInfo(long time, DateEnum type) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        if(type.equals(DateEnum.YEAR)){
            return calendar.get(Calendar.YEAR);
        }
        if(type.equals(DateEnum.SEASON)){
            int month = calendar.get(Calendar.MONTH);
            return month % 3 == 0 ? month / 3 : (month / 3 + 1);
        }
        if(type.equals(DateEnum.MONTH)){
            return calendar.get(Calendar.MONTH)+1;
        }
        if(type.equals(DateEnum.WEEK)){
            return calendar.get(Calendar.WEEK_OF_YEAR);
        }
        if(type.equals(DateEnum.DAY)){
            return calendar.get(Calendar.DAY_OF_YEAR);
        }
        if(type.equals(DateEnum.HOUR)){
            return calendar.get(Calendar.HOUR_OF_DAY);
        }
        throw new RuntimeException("不支持该类型的日期信息获取.type:"+type.dateType);
    }

    public static void main(String[] args) {
//        System.out.println(TimeUtil.isValidateDate("2018-08-17"));
//        System.out.println(TimeUtil.getYseterday());
//        System.out.println(TimeUtil.parseString2Long("2018-08-17"));
        System.out.println(TimeUtil.getDateInfo(1534435200000L,DateEnum.DAY ));
        System.out.println(TimeUtil.getDateInfo(1534435200000L,DateEnum.WEEK));
    }
}
