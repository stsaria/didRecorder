package si.f5.stsaria.didRecorder.Recorders;

import si.f5.stsaria.didRecorder.DidRecorderApplication;
import si.f5.stsaria.didRecorder.Hash;
import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.RecordFileControllers.UserAuthFC;
import si.f5.stsaria.didRecorder.RecordFileControllers.UserFC;
import si.f5.stsaria.didRecorder.Records.User;
import si.f5.stsaria.didRecorder.Records.UserAuth;
import si.f5.stsaria.didRecorder.TimeUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

public class UserR {
    private final long nowUnixTime;
    public UserR(){
        this.nowUnixTime = TimeUtils.getNowUnixTime();
    }
    public boolean exists(String id){
        ArrayList<User> users;
        try{users = UserFC.records();} catch (Exception ignore) {return false;}
        for (User user : users){
            if (user.id.equals(id)) return true;
        }
        return false;
    }
    public User getUser(String id){
        ArrayList<User> users;
        try{users = UserFC.records();} catch (Exception ignore) {return null;}
        for (User user : users){
            if (user.id.equals(id)) return user;
        }
        return null;
    }
    public boolean authForPass(String id, String pass) throws IOException {
        if (!this.exists(id)) {return false;}
        for (User userF : UserFC.records()){
            if (userF.id.equals(id) && userF.sha256Pass.equals(Hash.sha256Hex(pass))){
                return true;
            }
        }
        return false;
    }
    public boolean authForToken(String token) throws IOException {
        String[] spritCommaToken = token.split("-");
        if (spritCommaToken.length != 2) {return false;}
        if (!this.exists(spritCommaToken[0])) {return false;}
        for (UserAuth userAuth : UserAuthFC.records()){
            if (userAuth.user.id.equals(spritCommaToken[0]) &&
                userAuth.auth.equals(Hash.sha256Hex(spritCommaToken[1])) &&
                userAuth.limitUnixTime - this.nowUnixTime > 0)
            {
                return true;
            }
        }
        return false;
    }
    private User generateAndAppendUser(String name, String pass) throws IOException {
        String id = UUID.randomUUID().toString().replace("-", "");
        User user = new User(id, name, Hash.sha256Hex(pass), this.nowUnixTime, "default");
        UserFC.append(user.csvString);
        return user;
    }
    public UserAuth generateAndAppendAuthToken(User user) throws IOException {
        if (!this.exists(user.id)) return null;
        String auth = UUID.randomUUID().toString().replace("-", "");
        UserAuth userAuth = new UserAuth(user, auth, this.nowUnixTime, this.nowUnixTime+DidRecorderApplication.properties.getPropertyInt("tokenAuthLimitSeconds"));
        UserAuthFC.append(userAuth.csvString);
        return userAuth;
    }
    private boolean canAdd(String name, String pass) throws IOException {
        if (name.getBytes().length > DidRecorderApplication.properties.getPropertyInt("nameMaxByteSize") ||
            pass.getBytes().length > DidRecorderApplication.properties.getPropertyInt("passMaxByteSize") ||
            pass.getBytes().length < DidRecorderApplication.properties.getPropertyInt("passMinByteSize") ||
            !(name.matches("^[A-Za-z0-9]+$") && pass.matches("^[a-zA-Z0-9.?/-]+$")))
        {
            return false;
        }
        for (User user : UserFC.records()){
            if (user.name.equals(name)){
                return false;
            }
        }
        return true;
    }
    public String[] add(String name, String pass) throws IOException, NoSuchAlgorithmException {
        synchronized (FileLocks.user){
            if(this.canAdd(name, pass)){
                User user = this.generateAndAppendUser(name, pass);
                UserAuth auth = this.generateAndAppendAuthToken(user);
                return new String[]{user.id, auth.auth};
            }
        }
        return new String[]{"", ""};
    }
}
