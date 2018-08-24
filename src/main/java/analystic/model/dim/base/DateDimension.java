package analystic.model.dim.base;

import Util.TimeUtil;
import common.DateEnum;
import common.EventLogConstants;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DateDimension extends BaseDimension {

    private int id = 0;
    private int year = 0;
    private int season = 0;
    private int week = 0;
    private int day = 0;
    private int month = 0;
    private Date calendar = new Date();
    private String type = null;  //'日期格式'

    public DateDimension(int year, int season,int month, int week, int day) {
        this.year = year;
        this.season = season;
        this.month = month;
        this.week = week;
        this.day = day;
    }

    public DateDimension() {
    }

    public DateDimension(int year, int season, int month, int week, int day, String type ,Date calendar) {
        this(year,season,month,week,day);
        this.calendar = calendar;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Date getCalendar() {
        return calendar;
    }

    public void setCalendar(Date calendar) {
        this.calendar = calendar;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (o==this){
            return 0;
        }
        DateDimension other = (DateDimension) o;
        int tmp = this.id - other.id;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.year - other.year;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.season - other.season;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.month - other.month;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.week - other.week;
        if(tmp!=0){
            return tmp;
        }
        tmp = this.day - other.day;
        if(tmp!=0){
            return tmp;
        }
        return this.type.compareTo(other.type);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.day);
        dataOutput.writeInt(this.year);
        dataOutput.writeInt(this.week);
        dataOutput.writeInt(this.season);
        dataOutput.writeInt(this.month);
        dataOutput.writeInt(this.day);
        dataOutput.writeUTF(this.type);
        dataOutput.writeLong(this.calendar.getTime());
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.id = dataInput.readInt();
        this.year = dataInput.readInt();
        this.week = dataInput.readInt();
        this.season = dataInput.readInt();
        this.month = dataInput.readInt();
        this.day = dataInput.readInt();
        this.type = dataInput.readUTF();
        calendar.setTime(dataInput.readLong());
    }

    /**
     * 根据事件戳和指标类型获取对应时间维度对象
     * @param time
     * @param type
     * @return
     */
    public static DateDimension buildDate(long time,DateEnum type){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        int year = TimeUtil.getDateInfo(time,DateEnum.YEAR);
        if(type.equals(DateEnum.YEAR)){
            calendar.set(year,0,1);//年指标 截止日期为当年的1月1号
            return new DateDimension(year,0,0,0,1,type.dateType,calendar.getTime());
        }
        int season = TimeUtil.getDateInfo(time,DateEnum.SEASON);
        if(type.equals(DateEnum.SEASON)){
            int month = (season * 3 - 2 );
            calendar.set(year,month-1,1);//当前季度的第一个月1号
            return new DateDimension(year,season,month,0,1,type.dateType,calendar.getTime());
        }
        int month = TimeUtil.getDateInfo(time,DateEnum.MONTH);
        if(type.equals(DateEnum.MONTH)){
            calendar.set(year,month-1,1);//截至当月第一天
            return new DateDimension(year,season,month,0,1,type.dateType,calendar.getTime());
        }
        int week = TimeUtil.getDateInfo(time,DateEnum.WEEK);
        if(type.equals(DateEnum.WEEK)){
            long firstDavofWeek = TimeUtil.getFirsrDayOfWeek(time);
            year = TimeUtil.getDateInfo(firstDavofWeek,DateEnum.YEAR);
            season = TimeUtil.getDateInfo(firstDavofWeek,DateEnum.SEASON);
            month = TimeUtil.getDateInfo(firstDavofWeek,DateEnum.MONTH);
            calendar.set(year,month-1,1);//截至当月第一天
            return new DateDimension(year,season,month,week,0,type.dateType,calendar.getTime());
        }
        int day = TimeUtil.getDateInfo(time,DateEnum.DAY);
        if(type.equals(DateEnum.DAY)){
            calendar.set(year,month-1,day);//截至当月第一天
            return new DateDimension(year,season,month,week,day,type.dateType,calendar.getTime());
        }
        throw new RuntimeException("暂不支持该时间 维度time:"+time+"type:"+type.dateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,year,season,month,week,day,type,calendar);
    }
}
