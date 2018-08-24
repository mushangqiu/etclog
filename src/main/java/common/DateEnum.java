package common;

public enum DateEnum {
    YEAR("year"),
    SEASON("season"),
    MONTH("month"),
    WEEK("week"),
    DAY("day"),
    HOUR("hour");

    public String dateType;

    DateEnum(String dateType){
        this.dateType = dateType;
    }

}
