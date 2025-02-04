package si.f5.stsaria.didRecorder.RecordFileControllers;

import org.apache.commons.lang3.StringUtils;
import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.Did;
import si.f5.stsaria.didRecorder.Records.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DidFC {
    public static String read() throws IOException {
        Path path = Paths.get("records/did.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static void append(String string) throws IOException {
        File file = new File("records/did.record");
        FileWriter writer = new FileWriter(file, true);
        writer.write(string+"\n");
        writer.close();
    }
    public static ArrayList<ArrayList<Did>> recordsS() throws IOException {
        ArrayList<ArrayList<Did>> splitDayDids = new ArrayList<>();
        String[] recordSplitComma;
        for(String records : read().split("\n\n")){
            splitDayDids.add(new ArrayList<>());
            for(String record : records.split("\n")){
                record = record.strip();
                record = StringUtils.replaceEach(record, new String[]{"--..--", "\\n"}, new String[]{",", "\n"});
                recordSplitComma = record.split(",");
                if (recordSplitComma.length != 4) continue;
                if (!new UserR().exists(recordSplitComma[0])) continue;
                if (!recordSplitComma[1].matches("([0123])")) continue;
                try {Long.parseLong(recordSplitComma[1]);} catch (Exception e) {continue;}
                User user = new UserR().getUser(recordSplitComma[0]);
                if (recordSplitComma[1].matches("([03])")) {
                    try {Long.parseLong(recordSplitComma[3]);} catch (Exception e) {continue;}
                    splitDayDids.getLast().add(new Did(user, Integer.parseInt(recordSplitComma[1]), Long.parseLong(recordSplitComma[2]), Long.parseLong(recordSplitComma[3])));
                } else {
                    splitDayDids.getLast().add(new Did(user, Integer.parseInt(recordSplitComma[1]), Long.parseLong(recordSplitComma[2]), recordSplitComma[3]));
                }
            }
        }
        return splitDayDids;
    }
}
