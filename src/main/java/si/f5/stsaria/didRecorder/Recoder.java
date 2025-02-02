package si.f5.stsaria.didRecorder;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class Recoder {
    private final String userId;
    private final LocalDateTime nowTime;
    private final long nowUnixTime;
    private final String nowYMDTime;
    public Recoder(String userId){
        this.userId = userId;
        this.nowTime = TimeUtils.getNowLocalDateTime();
        this.nowUnixTime = TimeUtils.getNowUnixTime();
        this.nowYMDTime = TimeUtils.getNowYMDTime();
    }
    public int canAdd(String when, String content) throws IOException {
        if (!when.matches("[0123]")) return 1;
        else if (content.getBytes().length > 512) return 2;
        else if (content.split("\n").length > 5) return 3;
        else if (this.nowTime.getHour() < 10) return 4;
        else if (this.nowTime.getHour() > 18) return 5;
        int findCount = 0;
        for (String[] record : Objects.requireNonNull(Recoders.getLastUpdateDayFormatedDids(0))){
            if (Objects.equals(record[1], this.userId)){
                findCount++;
                if (findCount >= 4){
                    return 6;
                }
                if (Integer.parseInt(record[2]) > Integer.parseInt(when) || record[2].equals("3")){
                    return 7;
                }
                if (!Objects.equals(Recoders.readEndYMDF(), this.nowYMDTime)) {
                    return 0;
                }
                if (Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) > 54000){
                    return 0;
                }
            }
        }
        return 0;
    }
    public String nextWhen() throws IOException {
        int when = -1;
        if (!Objects.equals(Recoders.readEndYMDF(), this.nowYMDTime)) {
            return "0";
        }
        for (String[] record : Objects.requireNonNull(Recoders.getLastUpdateDayFormatedDids(0))){
            if (Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) < 54000 &&
                Objects.equals(record[1], this.userId))
            {
                when = Integer.parseInt(record[2]);
            }
        }
        when++;
        return String.valueOf(when);
    }
    public int add(String when, String content) throws IOException {
        content = StringUtils.replaceEach(content, new String[]{"--..--", "\\n"}, new String[]{",", "\n"});
        synchronized (Recoders.lock) {
            int canAddResult = canAdd(when, content);
            if (canAddResult == 0){
                content = StringUtils.replaceEach(content, new String[]{",", "\n"}, new String[]{"--..--", "\\n"});
                String record = this.nowUnixTime + "," + this.userId + "," + when + "," + content;
                if (!(Objects.equals(Recoders.readEndYMDF(), this.nowYMDTime) || Recoders.readDidsF().isEmpty())) {
                    record = "\n" + record;
                }
                Recoders.appendDidsF(record);
                Recoders.writeEndYMDF(this.nowYMDTime);
                return 0;
            }
            return canAddResult;
        }
    }
    public String getLatestLog(int gap) throws IOException {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder log = new StringBuilder();
        for (String[] record : Objects.requireNonNull(Recoders.getLastUpdateDayFormatedDids(gap))){
            if (!(Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) < 54000)){
                break;
            } else if (!Objects.equals(record[1], this.userId)){
                continue;
            }
            log
                .append(timeFormat.format(TimeUtils.unixTimeToLocalDateTime(Long.parseLong(record[0]), "Asia/Tokyo")))
                .append("\n")
                .append(StringUtils.replaceEach(
                        record[2],
                        new String[]{"0", "1", "2", "3"},
                        new String[]{"到着", "午前の記録", "午後の記録", "出発"}
                    )
                )
                .append("\n内容:")
                .append(record[2].matches("[03]")
                    ? timeFormat.format(TimeUtils.unixTimeToLocalDateTime(Long.parseLong(record[3]), "Asia/Tokyo"))
                    : record[3]
                )
                .append("\n\n");
        }
        return log.toString();
    }
}
