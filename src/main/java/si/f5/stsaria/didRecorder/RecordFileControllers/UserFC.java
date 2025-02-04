package si.f5.stsaria.didRecorder.RecordFileControllers;

import si.f5.stsaria.didRecorder.Records.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserFC {
    public static String read() throws IOException {
        Path path = Paths.get("records/user.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static void append(String string) throws IOException {
        File file = new File("records/user.record");
        FileWriter writer = new FileWriter(file, true);
        writer.write(string+"\n");
        writer.close();
    }
    public static ArrayList<User> records() throws IOException {
        ArrayList<User> users = new ArrayList<>();
        String[] recordSplitComma;
        for (String record : read().split("\n")){
            record = record.strip();
            recordSplitComma = record.split(",");
            if (recordSplitComma.length != 5) continue;
            try {Long.parseLong(recordSplitComma[3]);} catch (Exception e) {continue;}
            users.add(new User(recordSplitComma[0], recordSplitComma[1], recordSplitComma[2], Long.parseLong(recordSplitComma[3]), recordSplitComma[4]));
        }
        return users;
    }
}
