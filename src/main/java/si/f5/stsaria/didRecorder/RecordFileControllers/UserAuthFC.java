package si.f5.stsaria.didRecorder.RecordFileControllers;

import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.UserAuth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserAuthFC {
    public static String read() throws IOException {
        Path path = Paths.get("records/userAuth.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static void append(String string) throws IOException {
        File file = new File("records/userAuth.record");
        FileWriter writer = new FileWriter(file, true);
        writer.write(string+"\n");
        writer.close();
    }
    public static ArrayList<UserAuth> records() throws IOException {
        ArrayList<UserAuth> userAuths = new ArrayList<>();
        String[] recordSplitComma;
        for (String record : read().split("\n")){
            record = record.strip();
            recordSplitComma = record.split(",");
            if (recordSplitComma.length != 4) continue;
            if (!new UserR().exists(recordSplitComma[0])) continue;
            try {
                Long.parseLong(recordSplitComma[2]);
                Long.parseLong(recordSplitComma[3]);
            } catch (Exception ignore) {continue;}
            userAuths.add(new UserAuth(new UserR().getUser(recordSplitComma[0]), recordSplitComma[1], Long.parseLong(recordSplitComma[2]), Long.parseLong(recordSplitComma[3])));
        }
        return userAuths;
    }
}
