package si.f5.stsaria.didRecorder.RecordFileControllers;

import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.RealName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RealNameFC {
    public static String read() throws IOException {
        Path realNamesFilePath = Paths.get("records/realName.record");
        if (realNamesFilePath.toFile().isFile()) {
            return Files.readString(realNamesFilePath);
        } else {
            return "";
        }
    }
    public static void append(String string) throws IOException {
        File file = new File("records/realName.record");
        FileWriter writer = new FileWriter(file, true);
        writer.write(string+"\n");
        writer.close();
    }
    public static ArrayList<RealName> records() throws IOException {
        ArrayList<RealName> realNames = new ArrayList<>();
        String[] recordSplitComma;
        for (String record : read().split("\n")){
            record = record.strip();
            recordSplitComma = record.split(",");
            if (recordSplitComma.length != 3) continue;
            if (!new UserR().exists(recordSplitComma[0])) continue;
            try {Long.parseLong(recordSplitComma[2]);} catch (Exception e) {continue;}
            realNames.add(new RealName(new UserR().getUser(recordSplitComma[0]), recordSplitComma[1], Long.parseLong(recordSplitComma[2])));
        }
        return realNames;
    }
}
