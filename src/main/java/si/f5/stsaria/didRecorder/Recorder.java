package si.f5.stsaria.didRecorder;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Objects;


public class Recorder {
    private final String userId;
    private final LocalDateTime nowTime;
    private final long nowUnixTime;
    private final String nowYMDTime;
    public Recorder(String userId){
        this.userId = userId;
        this.nowTime = TimeUtils.getNowLocalDateTime();
        this.nowUnixTime = TimeUtils.getNowUnixTime();
        this.nowYMDTime = TimeUtils.unixTimeToYMD(this.nowUnixTime, "Asia/Tokyo");
    }
    public int canAdd(String when, String content) throws IOException {
        if (!when.matches("[0123]")) return 1;
        else if (content.getBytes().length > DidRecorderApplication.properties.getPropertyInt("maxRecordContentByteSize")) return 2;
        else if (content.split("\n").length > DidRecorderApplication.properties.getPropertyInt("maxRecordLines")) return 3;
        else if (this.nowTime.getHour() < DidRecorderApplication.properties.getPropertyInt("minTimeHours")) return 4;
        else if (this.nowTime.getHour() > DidRecorderApplication.properties.getPropertyInt("maxTimeHours")) return 5;
        int findCount = 0;
        for (String[] record : Objects.requireNonNull(Recorders.getLastUpdateDayFormatedDids(0))){
            if (Objects.equals(record[1], this.userId)){
                findCount++;
                if (findCount >= 4){
                    return 6;
                }
                if (Integer.parseInt(record[2]) > Integer.parseInt(when) || record[2].equals("3")){
                    return 7;
                }
                if (!Objects.equals(Recorders.readEndYMDF(), this.nowYMDTime)) {
                    return 0;
                }
                if (Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) > DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")){
                    return 0;
                }
            }
        }
        return 0;
    }
    public String nextWhen() throws IOException {
        int when = -1;
        if (!Objects.equals(Recorders.readEndYMDF(), this.nowYMDTime)) {
            return "0";
        }
        for (String[] record : Objects.requireNonNull(Recorders.getLastUpdateDayFormatedDids(0))){
            if (Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) < DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds") &&
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
        synchronized (Recorders.lock) {
            int canAddResult = canAdd(when, content);
            if (canAddResult == 0){
                content = StringUtils.replaceEach(content, new String[]{",", "\n"}, new String[]{"--..--", "\\n"});
                String record = this.nowUnixTime + "," + this.userId + "," + when + "," + content;
                if (!(Objects.equals(Recorders.readEndYMDF(), this.nowYMDTime) || Recorders.readDidsF().isEmpty())) {
                    record = "\n" + record;
                }
                Recorders.appendDidsF(record);
                Recorders.writeEndYMDF(this.nowYMDTime);
                return 0;
            }
            return canAddResult;
        }
    }
    public String getLatestLog(int gap) throws IOException {
        StringBuilder log = new StringBuilder();
        for (String[] record : Objects.requireNonNull(Recorders.getLastUpdateDayFormatedDids(gap))){
            if (!(Math.abs(Integer.parseInt(record[0]) - this.nowUnixTime) < (gap == 0 ?
                    DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")
                : DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")*(long) gap))){
                break;
            } else if (!Objects.equals(record[1], this.userId)){
                continue;
            }
            log
                .append(TimeUtils.unixTimeToHM(Long.parseLong(record[0]), "Asia/Tokyo"))
                .append("\n")
                .append(StringUtils.replaceEach(
                        record[2],
                        new String[]{"0", "1", "2", "3"},
                        new String[]{"到着", "午前の記録", "午後の記録", "出発"}
                    )
                )
                .append("\n内容:")
                .append(record[2].matches("[03]")
                    ? TimeUtils.unixTimeToHM(Long.parseLong(record[3]), "Asia/Tokyo")
                    : record[3]
                )
                .append("\n\n");
        }
        return log.toString();
    }
}
