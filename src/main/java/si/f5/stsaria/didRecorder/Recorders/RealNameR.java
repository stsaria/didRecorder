package si.f5.stsaria.didRecorder.Recorders;

import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.RecordFileControllers.RealNameFC;
import si.f5.stsaria.didRecorder.Records.RealName;
import si.f5.stsaria.didRecorder.Records.User;
import si.f5.stsaria.didRecorder.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;

public class RealNameR {
    private final long nowUnixTime;
    public RealNameR(){
        this.nowUnixTime = TimeUtils.getNowUnixTime();
    }
    public boolean existsUser(User user){
        ArrayList<RealName> realNames;
        try{realNames = RealNameFC.records();} catch (Exception ignore) {return false;}
        for (RealName realName : realNames){
            if (realName.user.id.equals(user.id)) return true;
        }
        return false;
    }
    public boolean existsName(String name){
        ArrayList<RealName> realNames;
        try{realNames = RealNameFC.records();} catch (Exception ignore) {return false;}
        for (RealName realName : realNames){
            if (realName.name.equals(name)) return true;
        }
        return false;
    }
    public RealName getRealName(User user){
        ArrayList<RealName> realNames;
        try{realNames = RealNameFC.records();} catch (Exception ignore) {return null;}
        for (RealName realName : realNames){
            if (realName.user.id.equals(user.id)) return realName;
        }
        return null;
    }
    private int canAdd(User user, String name){
        if (!new UserR().exists(user.id)) return 1;
        else if (name.getBytes().length > 30) return 2;
        if (existsUser(user) || existsName(name)) return 3;
        return 0;
    }
    public int add(User user, String name) throws IOException {
        name = name.replace(",", "");
        synchronized (FileLocks.realName){
            int result = this.canAdd(user, name);
            if(result == 0){
                RealName realName = new RealName(user, name, this.nowUnixTime);
                RealNameFC.append(realName.csvString);
                return 0;
            }
            return result;
        }
    }
}
