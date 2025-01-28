package si.f5.stsaria.didRecorder;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recoder {
    private static final Object lock = new Object();
    private final String userId;
    public Recoder(String userId){
        this.userId = userId;
    }
    private String readEndYMDF() throws IOException {
        Path endTimeFilePath = Paths.get("endYMD.record");
        if (endTimeFilePath.toFile().isFile()) {
            return Files.readString(endTimeFilePath);
        } else {
            return null;
        }
    }
    private String readDidsF() throws IOException {
        Path didsFilePath = Paths.get("dids.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return null;
        }
    }
    private void writeEndYMDF(String string) throws IOException {
        Path endTimeFilePath = Paths.get("endYMD.record");
        FileWriter endTimeWriter = new FileWriter(endTimeFilePath.toFile());
        endTimeWriter.write(string);
        endTimeWriter.close();
    }
    private void appendDidsF(String string) throws IOException {
        File didsFile = new File("dids.record");
        PrintWriter didsWriter = new PrintWriter(new BufferedWriter(new FileWriter(didsFile)));
        didsWriter.println(string);
        didsWriter.close();
    }
    private ArrayList<ArrayList<String[]>> getFormatedDidsS() throws IOException {
        ArrayList<ArrayList<String[]>> recordsS = new ArrayList<>();
        String readedRecords = readDidsF();
        if (readedRecords == null){
            return null;
        }
        String[] allRecordSplitDay = readedRecords.split("\n\n");
        for(String records : allRecordSplitDay){
            recordsS.add(new ArrayList<>());
            for(String record : records.split("\n")){
                if (record.split(",").length != 5){
                    continue;
                }
                recordsS.getLast().add(record.split(","));
            }
        }
        return recordsS;
    }
    private ArrayList<String[]> getLastUpdateDayFormatedDids() throws IOException {
        ArrayList<ArrayList<String[]>> recordsS = Objects.requireNonNull(this.getFormatedDidsS());
        if (recordsS.isEmpty()){
            return new ArrayList<>(List.of());
        }
        return recordsS.getLast();
    }
    private boolean canAdd(String when, int comeOrGoUnixTime, String content) throws IOException {
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        int comeOrGoHour = LocalDateTime.ofInstant(Instant.ofEpochSecond(comeOrGoUnixTime), ZoneId.systemDefault()).atZone(ZoneId.of("Asia/Tokyo")).getHour();
        if (!(when.equals("am") || when.equals("pm")) || !(comeOrGoHour >= 10 && comeOrGoHour <= 18) || content.getBytes().length > 1024 || content.split("\n").length > 5){
            return false;
        }
        int findCount = 0;
        for (String[] record : Objects.requireNonNull(getLastUpdateDayFormatedDids())){
            if (Objects.equals(record[1], this.userId)){
                findCount++;
                if (findCount > 2){
                    return false;
                } else if (!Objects.equals(this.readEndYMDF(), nowTime.format(timeFormat))) {
                    return true;
                } else if (Math.abs(Integer.parseInt(record[1]) - comeOrGoUnixTime) > 54000){
                    return true;
                } else if (when.equals("am")){
                    return false;
                } else if (record[2].equals("am")){
                    return true;
                }
            }
        }
        return true;
    }
    public boolean add(String when, int comeOrGoUnixTime, String content) throws IOException {
        content = StringUtils.replaceEach(content, new String[]{",", "\n"}, new String[]{"--..--", "\\n"});
        synchronized (lock) {
            if (canAdd(when, comeOrGoUnixTime, content)){
                LocalDateTime nowTime = LocalDateTime.now();
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                String dataOfAdd = nowTime.atZone(ZoneId.systemDefault()).toEpochSecond() + "," + this.userId + "," + when + "," + comeOrGoUnixTime + "," + content;
                if (!Objects.equals(this.readEndYMDF(), nowTime.format(timeFormat))) {
                    dataOfAdd = "\n" + dataOfAdd;
                }
                this.appendDidsF(dataOfAdd);
                this.writeEndYMDF(nowTime.format(timeFormat));
                return true;
            }
        }
        return false;
    }
}
