package si.f5.stsaria.didRecorder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {
    private static final Object lock = new Object();
    private String sha256Hex(String string) throws NoSuchAlgorithmException {
        return HexFormat.of().withLowerCase().formatHex(MessageDigest.getInstance("SHA-256").digest(string.getBytes()));
    }
    private String readUsersF() throws IOException {
        Path didsFilePath = Paths.get("users.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return "";
        }
    }
    private String readUserAuthsF() throws IOException {
        Path didsFilePath = Paths.get("userAuths.record");
        if (didsFilePath.toFile().isFile()) {
            return Files.readString(didsFilePath);
        } else {
            return "";
        }
    }
    private ArrayList<String[]> getFormatedUsers() throws IOException {
        ArrayList<String[]> users = new ArrayList<>();
        for(String user : readUsersF().split("\n")){
            if (user.split(",").length != 4){
                continue;
            }
            users.add(user.split(","));
        }
        return users;
    }
    private ArrayList<String[]> getFormatedUserAuths() throws IOException {
        ArrayList<String[]> userAuths = new ArrayList<>();
        for(String userAuth : readUserAuthsF().split("\n")){
            if (userAuth.split(",").length != 2){
                continue;
            }
            userAuths.add(userAuth.split(","));
        }
        return userAuths;
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
    private String generateAndAppendUser(String name, String pass) throws IOException, NoSuchAlgorithmException {
        String userId = UUID.randomUUID().toString().replace("-", "");
        appendUsersF(userId+","+name+","+this.sha256Hex(pass)+","+LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        return userId;
    }
    public String generateAndAppendAuthToken(String userId) throws IOException {
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
        appendUserAuthsF(generatedAuthID+","+LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toEpochSecond()+1.728e+6);
        return generatedAuthID;
    }
    private boolean canAdd(String name, String authPass) throws IOException {
        if (!Pattern.compile("^[A-Za-z0-9]+$").matcher(name).matches() && authPass.length() < 3){
            return false;
        }
        for (String[] user : this.getFormatedUsers()){
            if (user[1].equals(name)){
                return false;
            }
        }
        return true;
    }
    public boolean authForPass(String userId, String pass) throws IOException, NoSuchAlgorithmException {
        for (String[] user : this.getFormatedUsers()){
            if (user[0].equals(userId) && user[2].equals(sha256Hex(pass))){
                return true;
            }
        }
        return false;
    }
    public boolean authForToken(String token) throws IOException {
        for (String[] userAuth : this.getFormatedUserAuths()){
            if (Objects.equals(userAuth[0], token) && Integer.parseInt(userAuth[1]) - LocalDateTime.now().atZone(ZoneId.of("Asia/Tokyo")).toEpochSecond() > 0){
                return true;
            }
        }
        return false;
    }
    public String[] add(String name, String authPass) throws IOException, NoSuchAlgorithmException {
        synchronized (lock){
            if(this.canAdd(name, authPass)){
                String userId = this.generateAndAppendUser(name, authPass);
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
