package si.f5.stsaria.didRecorder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

public class User {
    private static final Object lock = new Object();
    private String readUsersF() throws IOException {
        Path didsFilePath = Paths.get("dids.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return "";
        }
    }
    private ArrayList<String[]> getFormatedUsers() throws IOException {
        ArrayList<String[]> users = new ArrayList<>();
        for(String user : readUsersF().split("\n")){
            if (user.split(",").length != 3){
                continue;
            }
            users.add(user.split(","));
        }
        return users;
    }
    private void appendUsersF(String string) throws IOException {
        File usersFile = new File("users.record");
        PrintWriter usersWriter = new PrintWriter(new BufferedWriter(new FileWriter(usersFile)));
        usersWriter.println(string);
        usersWriter.close();
    }
    private void appendUserAuthsF(String string) throws IOException {
        File usersFile = new File("userAuths.record");
        PrintWriter usersWriter = new PrintWriter(new BufferedWriter(new FileWriter(usersFile)));
        usersWriter.println(string);
        usersWriter.close();
    }
    private String generateAndAppendUser(String name) throws IOException {
        String userId = UUID.randomUUID().toString();
        appendUsersF(userId+","+name+","+LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        return userId;
    }
    private String generateAndAppendAuthID(){
        boolean founded = false;

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
