package si.f5.stsaria.didRecorder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recorders {
    public static final Object lock = new Object();
    public static String readEndYMDF() throws IOException {
        Path endTimeFilePath = Paths.get("records/endYMD.record");
        if (endTimeFilePath.toFile().isFile()) {
            return Files.readString(endTimeFilePath);
        } else {
            return "";
        }
    }
    public static String readDidsF() throws IOException {
        Path didsFilePath = Paths.get("records/dids.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return "";
        }
    }
    public static void writeEndYMDF(String string) throws IOException {
        Path endTimeFilePath = Paths.get("records/endYMD.record");
        FileWriter endTimeWriter = new FileWriter(endTimeFilePath.toFile());
        endTimeWriter.write(string);
        endTimeWriter.close();
    }
    public static void appendDidsF(String string) throws IOException {
        File didsFile = new File("records/dids.record");
        FileWriter didsWriter = new FileWriter(didsFile, true);
        didsWriter.write(string+"\n");
        didsWriter.close();
    }
    public static ArrayList<ArrayList<String[]>> getFormatedDidsS() throws IOException {
        ArrayList<ArrayList<String[]>> recordsS = new ArrayList<>();
        for(String records : readDidsF().split("\n\n")){
            recordsS.add(new ArrayList<>());
            for(String record : records.split("\n")){
                record = record.strip();
                record = StringUtils.replaceEach(record, new String[]{"--..--", "\\n"}, new String[]{",", "\n"});
                if (record.split(",").length != 4){
                    continue;
                }
                recordsS.getLast().add(record.split(","));
            }
        }
        return recordsS;
    }
    public static ArrayList<String[]> getLastUpdateDayFormatedDids(int gap) throws IOException {
        ArrayList<ArrayList<String[]>> recordsS = Objects.requireNonNull(Recorders.getFormatedDidsS());
        if ((recordsS.size() - 1 - gap) < 0){

            return new ArrayList<>(List.of());
        }
        return recordsS.get(recordsS.size() - 1 - gap);
    }
    public static String getLatestLog(int gap, int type) throws IOException {
        StringBuilder log = new StringBuilder();
        ArrayList<String[]> dids = Objects.requireNonNull(Recorders.getLastUpdateDayFormatedDids(gap));
        ArrayList<String> foundUserIds = new ArrayList<>();
        synchronized (Users.lock) {
            for (String[] record : dids) {
                if (!(Math.abs(Integer.parseInt(record[0]) - TimeUtils.getNowUnixTime()) < (gap == 0 ? 54000 : 54000L *gap))) {
                    break;
                }
                switch (type) {
                    case 0:
                        log
                            .append(TimeUtils.unixTimeToHM(Long.parseLong(record[0]), "Asia/Tokyo"))
                            .append(",")
                            .append(new RealNames().getName(record[1]).isEmpty() ?
                                        new Users().getName(record[1])
                                    : new RealNames().getName(record[1]))
                            .append(",")
                            .append(StringUtils.replaceEach(
                                    record[2],
                                    new String[]{"0", "1", "2", "3"},
                                    new String[]{"到着", "午前の記録", "午後の記録", "出発"}
                                )
                            )
                            .append(",")
                            .append(record[2].matches("[03]") ?
                                TimeUtils.unixTimeToHM(Long.parseLong(record[3]), "Asia/Tokyo")
                                : record[3].replace("\n", " ")
                            )
                            .append("\n");
                        break;
                    case 1:
                        if (!foundUserIds.contains(record[1])){
                            log.append(record[2].equals("0") ?
                                new RealNames().getName(record[1]).isEmpty() ?
                                    new Users().getName(record[1])
                                : new RealNames().getName(record[1])
                            : "").append("\n");
                            foundUserIds.add(record[1]);
                        }
                        break;
                }
            }
        }
        return log.toString();
    }
}
