package si.f5.stsaria.didRecorder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RealNames {
    public static final Object lock = new Object();
    private String readRealNamesF() throws IOException {
        Path realNamesFilePath = Paths.get("records/realNames.record");
        if (realNamesFilePath.toFile().isFile()) {
            return Files.readString(realNamesFilePath);
        } else {
            return "";
        }
    }
    public static void appendRealNamesF(String string) throws IOException {
        File realNamesFile = new File("records/realNames.record");
        FileWriter realNamesWriter = new FileWriter(realNamesFile, true);
        realNamesWriter.write(string+"\n");
        realNamesWriter.close();
    }
    public ArrayList<String[]> getFormatedRealNames() throws IOException {
        ArrayList<String[]> users = new ArrayList<>();
        for(String user : readRealNamesF().split("\n")){
            user = user.strip();
            if (user.split(",").length != 3){
                continue;
            }
            users.add(user.split(","));
        }
        return users;
    }
    public String getName(String id) throws IOException {
        for (String[] nameInfo : this.getFormatedRealNames()){
            if (nameInfo[1].equals(id)){
                return nameInfo[0];
            }
        }
        return "";
    }
    private int canAdd(String userId, String name) throws IOException {
        if (!new Users().exists(userId)) return 1;
        else if (name.getBytes().length > 30) return 2;
        for (String[] nameInfo : this.getFormatedRealNames()){
            if (nameInfo[0].equals(name) || nameInfo[1].equals(userId)){
                return 3;
            }
        }
        return 0;
    }
    public int add(String userId, String name) throws IOException {
        synchronized (lock){
            int result = this.canAdd(userId, name);
            if(result == 0){
                appendRealNamesF(name+","+userId+","+TimeUtils.getNowUnixTime());
                return 0;
            }
            return result;
        }
    }
}
