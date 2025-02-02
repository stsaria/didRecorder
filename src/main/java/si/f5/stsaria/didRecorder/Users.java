package si.f5.stsaria.didRecorder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Users {
    public static final Object lock = new Object();
    private final long nowUnixTime;
    public Users(){
        this.nowUnixTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }
    private String sha256Hex(String string) throws NoSuchAlgorithmException {
        return HexFormat.of().withLowerCase()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(string.getBytes()));
    }
    private String readUsersF() throws IOException {
        Path usersFilePath = Paths.get("records/users.record");
        if (usersFilePath.toFile().isFile()) {
            return Files.readString(usersFilePath);
        } else {
            return "";
        }
    }
    private String readUserAuthsF() throws IOException {
        Path userAuthsFilePath = Paths.get("records/userAuths.record");
        if (userAuthsFilePath.toFile().isFile()) {
            return Files.readString(userAuthsFilePath);
        } else {
            return "";
        }
    }
    public ArrayList<String[]> getFormatedUsers() throws IOException {
        ArrayList<String[]> users = new ArrayList<>();
        for(String user : readUsersF().split("\n")){
            user = user.strip();
            if (user.split(",").length != 5){
                continue;
            }
            users.add(user.split(","));
        }
        return users;
    }
    private ArrayList<String[]> getFormatedUserAuths() throws IOException {
        ArrayList<String[]> userAuths = new ArrayList<>();
        for(String userAuth : readUserAuthsF().split("\n")){
            userAuth = userAuth.strip();
            if (userAuth.split(",").length != 2){
                continue;
            }
            userAuths.add(userAuth.split(","));
        }
        return userAuths;
    }
    private void appendUsersF(String string) throws IOException {
        File usersFile = new File("records/users.record");
        FileWriter usersWriter = new FileWriter(usersFile, true);
        usersWriter.write(string+"\n");
        usersWriter.close();
    }
    private void appendUserAuthsF(String string) throws IOException {
        File usersFile = new File("records/userAuths.record");
        FileWriter userAuthsWriter = new FileWriter(usersFile, true);
        userAuthsWriter.write(string+"\n");
        userAuthsWriter.close();
    }
    private String generateAndAppendUser(String name, String pass) throws IOException, NoSuchAlgorithmException {
        String userId = UUID.randomUUID().toString().replace("-", "");
        appendUsersF(userId+","+name+","+this.sha256Hex(pass)+","+this.nowUnixTime+",default");
        return userId;
    }
    public String generateAndAppendAuthToken(String userId) throws IOException, NoSuchAlgorithmException {
        String generatedAuthID;
        String[] targetUser = null;
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(userId)){
                targetUser = user;
                break;
            }
        }
        if (targetUser == null) {
            return "";
        }
        generatedAuthID = targetUser[0]+"."+UUID.randomUUID().toString().replace("-", "");
        appendUserAuthsF(this.sha256Hex(generatedAuthID)+","+(this.nowUnixTime+1728000));
        return generatedAuthID;
    }
    private boolean canAdd(String name, String pass) throws IOException {
        if (name.getBytes().length > 18 ||
            pass.getBytes().length > 16 ||
            !(name.matches("^[A-Za-z0-9]+$") && pass.matches("^[a-zA-Z0-9.?/-]$")) ||
            pass.length() < 3)
        {
            return false;
        }
        for (String[] user : this.getFormatedUsers()){
            if (user[1].equals(name)){
                return false;
            }
        }
        return true;
    }
    public boolean exists(String id) throws IOException {
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(id)){
                return true;
            }
        }
        return false;
    }
    public boolean isAdmin(String id) throws IOException {
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(id) && user[4].equals("admin")){
                return true;
            }
        }
        return false;
    }
    public String getName(String id) throws IOException {
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(id)){
                return user[1];
            }
        }
        return "";
    }
    public boolean authForPass(String id, String pass) throws IOException, NoSuchAlgorithmException {
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(id) && user[2].equals(sha256Hex(pass))){
                return true;
            }
        }
        return false;
    }
    public boolean authForToken(String token) throws IOException, NoSuchAlgorithmException {
        for (String[] userAuth : this.getFormatedUserAuths()){
            if (userAuth[0].equals(this.sha256Hex(token)) && Long.parseLong(userAuth[1]) - this.nowUnixTime > 0){
                return true;
            }
        }
        return false;
    }
    public String[] add(String name, String pass) throws IOException, NoSuchAlgorithmException {
        synchronized (lock){
            if(this.canAdd(name, pass)){
                String userId = this.generateAndAppendUser(name, pass);
                String token = this.generateAndAppendAuthToken(userId);
                if (userId.isEmpty() || token.isEmpty()){
                    return new String[]{"", ""};
                }
                return new String[]{userId, token};
            }
        }
        return new String[]{"", ""};
    }
}
