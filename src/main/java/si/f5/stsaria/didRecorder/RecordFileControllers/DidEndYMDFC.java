package si.f5.stsaria.didRecorder.RecordFileControllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DidEndYMDFC {
    public static String read() throws IOException {
        Path path = Paths.get("records/didEndYMD.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static void write(String string) throws IOException {
        File file = new File("records/didEndYMD.record");
        FileWriter writer = new FileWriter(file);
        writer.write(string);
        writer.close();
    }
}
