package si.f5.stsaria.didRecorder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class User {
    private static final Object lock = new Object();
    private String readUsersF() throws IOException {
        Path didsFilePath = Paths.get("dids.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return null;
        }
    }
    private ArrayList<ArrayList<String[]>> getFormatedRecordsS() throws IOException {
        ArrayList<ArrayList<String[]>> recordsS = new ArrayList<>();
        String readedRecords = readUsersF();
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
    private void appendUsersF(String string) throws IOException {
        File usersFile = new File("users.record");
        PrintWriter usersWriter = new PrintWriter(new BufferedWriter(new FileWriter(usersFile)));
        usersWriter.println(string);
        usersWriter.close();
    }
    private String generateUserID(){
        return "";
    }
    private String generateAndAppendAuthID(){
        return "";
    }
    private boolean canAdd(String name, String authPass){
        return true;
    }
    public boolean auth(String token){
        return true;
    }
    public String add(String name, String authPass){
        synchronized (lock){
            if(this.canAdd(name, authPass)){

                return "";
            }
        }
        return "";
    }
}
