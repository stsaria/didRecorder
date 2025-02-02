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
    public static String getNowYMDTime(){
        return TimeUtils.getNowLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    public static LocalDateTime unixTimeToLocalDateTime(long time, String zoneId){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
                .atZone(ZoneId.of(zoneId)).toLocalDateTime();
    }
    public static String unixTimeToHM(long time, String zoneId){
        return DateTimeFormatter.ofPattern("HH:mm").format(TimeUtils.unixTimeToLocalDateTime(time, zoneId));
    }
    public static long hMTimeToUnixTime(String time){
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                .atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
