package si.f5.stsaria.didRecorder;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static LocalDateTime getNowLocalDateTime(){
        return LocalDateTime.now();
    }
    public static long getNowUnixTime(){
        return TimeUtils.getNowLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond();
    }
    public static LocalDateTime unixTimeToLocalDateTime(long time, String zoneId){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
                .atZone(ZoneId.of(zoneId)).toLocalDateTime();
    }
    private static String unixTimeToFormat(long time, String format, String zoneId){
        return DateTimeFormatter.ofPattern(format)
                .format(unixTimeToLocalDateTime(time, zoneId));
    }
    public static String unixTimeToHM(long time, String zoneId){
        return unixTimeToFormat(time, "HH:mm", zoneId);
    }
    public static String unixTimeToYMD(long time, String zoneId){
        return unixTimeToFormat(time, "yyyy/MM/dd", zoneId);
    }
    public static String unixTimeToYMDHM(long time, String zoneId){
        return unixTimeToFormat(time, "yyyy/MM/dd - HH:mm", zoneId);
    }
    public static long hMTimeToUnixTime(String time){
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                .atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
