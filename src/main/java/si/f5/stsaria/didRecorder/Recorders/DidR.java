package si.f5.stsaria.didRecorder.Recorders;

import org.apache.commons.lang3.StringUtils;
import si.f5.stsaria.didRecorder.*;
import si.f5.stsaria.didRecorder.RecordFileControllers.DidEndYMDFC;
import si.f5.stsaria.didRecorder.RecordFileControllers.DidFC;
import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.Records.Did;
import si.f5.stsaria.didRecorder.Records.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DidR {
    private final LocalDateTime nowTime;
    private final long nowUnixTime;
    private final String nowYMDTime;
    public DidR(){
        this.nowTime = TimeUtils.getNowLocalDateTime();
        this.nowUnixTime = TimeUtils.getNowUnixTime();
        this.nowYMDTime = TimeUtils.unixTimeToYMD(this.nowUnixTime, "Asia/Tokyo");
    }
    private ArrayList<Did> lastUpdateDids(int gap) throws IOException {
        ArrayList<ArrayList<Did>> recordsS = DidFC.recordsS();
        if ((recordsS.size() - 1 - gap) < 0){
            return new ArrayList<>(List.of());
        }
        return recordsS.get(recordsS.size() - 1 - gap);
    }
    public int canAdd(User user, String type, String content) throws IOException {
        if (!type.matches("[0123]")) return 1;
        else if (content.getBytes().length > DidRecorderApplication.properties.getPropertyInt("maxRecordContentByteSize")) return 2;
        else if (content.split("\n").length > DidRecorderApplication.properties.getPropertyInt("maxRecordLines")) return 3;
        else if (this.nowTime.getHour() < DidRecorderApplication.properties.getPropertyInt("minTimeHours")) return 4;
        else if (this.nowTime.getHour() > DidRecorderApplication.properties.getPropertyInt("maxTimeHours")) return 5;
        int findCount = 0;
        for (Did did : this.lastUpdateDids(0)){
            if (Objects.equals(did.user, user)){
                findCount++;
                if (findCount >= 4){
                    return 6;
                }
                if (did.type > Integer.parseInt(type) || did.type == 3){
                    return 7;
                }
                if (!DidEndYMDFC.read().equals(this.nowYMDTime)) {
                    return 0;
                }
                if (Math.abs(did.makeUnixTime - this.nowUnixTime) > DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")){
                    return 0;
                }
            }
        }
        return 0;
    }
    public int nextWhen(User user) throws IOException {
        int when = -1;
        UserR userR = new UserR();
        if (!userR.exists(user.id)) return 4;
        if (!Objects.equals(DidEndYMDFC.read(), this.nowYMDTime)) {
            return 0;
        }
        for (Did did : lastUpdateDids(0)){
            if (did.user.id.equals(user.id) &&
                Math.abs(did.makeUnixTime - this.nowUnixTime) < DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds"))
            {
                when = did.type;
            }
        }
        when++;
        return when;
    }
    public int add(User user, String type, String content) throws IOException {
        content = StringUtils.replaceEach(content, new String[]{"--..--", "\\n"}, new String[]{",", "\n"});
        synchronized (FileLocks.did) {
            int canAddResult = canAdd(user, type, content);
            if (canAddResult == 0){
                content = StringUtils.replaceEach(content, new String[]{",", "\n"}, new String[]{"--..--", "\\n"});
                Did did = type.matches("[03]") ?
                    new Did(user, Integer.parseInt(type), this.nowUnixTime, Long.parseLong(content))
                    : new Did(user, Integer.parseInt(type), this.nowUnixTime, content);
                if (!(DidEndYMDFC.read().equals(this.nowYMDTime))) DidFC.append("");
                DidFC.append(did.csvString);
                DidEndYMDFC.write(this.nowYMDTime);
                return 0;
            }
            return canAddResult;
        }
    }
    public String getLatestAllLog(int gap, int type) throws IOException {
        StringBuilder log = new StringBuilder();
        ArrayList<String> foundUserIds = new ArrayList<>();
        synchronized (FileLocks.user) {
            for (Did did : this.lastUpdateDids(gap)) {
                switch (type) {
                    case 0:
                        log
                            .append(TimeUtils.unixTimeToYMDHM(did.makeUnixTime, "Asia/Tokyo"))
                            .append(",")
                            .append(new RealNameR().existsUser(did.user) ?
                                new RealNameR().getRealName(did.user).name
                            : did.user.name)
                            .append(",")
                            .append(StringUtils.replaceEach(
                                    String.valueOf(did.type),
                                    new String[]{"0", "1", "2", "3"},
                                    new String[]{"到着", "午前の記録", "午後の記録", "出発"}
                                )
                            )
                            .append(",")
                            .append(String.valueOf(did.type).matches("[03]") ?
                                    TimeUtils.unixTimeToHM(did.contentUnixTime, "Asia/Tokyo")
                                    : did.contentString.replace("\n", " ")
                            )
                            .append("\n");
                        break;
                    case 1:
                        if (!foundUserIds.contains(did.user.id) && did.type == 0){
                            log.append(new RealNameR().existsUser(did.user) ?
                                new RealNameR().getRealName(did.user).name
                            : did.user.name)
                            .append(",").append(TimeUtils.unixTimeToHM(did.contentUnixTime, "Asia/Tokyo"));
                            foundUserIds.add(did.user.id);
                        }
                        break;
                }
            }
        }
        return log.toString();
    }
    public String getLatestUserLog(User user, int gap, int type) throws IOException {
        StringBuilder log = new StringBuilder();
        for (Did did : this.lastUpdateDids(gap)){
            if (!(Math.abs(did.makeUnixTime - this.nowUnixTime) < (gap == 0 ?
                DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")
                : DidRecorderApplication.properties.getPropertyInt("dayChangeThresholdSeconds")*(long) gap)))
            {
                break;
            } else if (!did.user.id.equals(user.id)){
                continue;
            }
            switch (type) {
                case 0:
                    log
                        .append(TimeUtils.unixTimeToHM(did.makeUnixTime, "Asia/Tokyo"))
                        .append("\n")
                        .append(StringUtils.replaceEach(
                                String.valueOf(did.type),
                                new String[]{"0", "1", "2", "3"},
                                new String[]{"到着", "午前の記録", "午後の記録", "出発"}
                            )
                        )
                        .append("\n内容:")
                        .append(String.valueOf(did.type).matches("[03]")?
                            TimeUtils.unixTimeToHM(did.contentUnixTime, "Asia/Tokyo")
                        : did.contentString)
                        .append("\n\n");
                case 1:
                    log.append(did.type == 0 ? TimeUtils.unixTimeToHM(did.contentUnixTime, "Asia/Tokyo")+"\n" : "");
            }
        }
        return log.toString();
    }
}
