package si.f5.stsaria.didRecorder.checker;

import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.User;

public class Login {
    public static boolean loginChecker(String token) {
        try {
            synchronized (FileLocks.user) {
                if (new UserR().authForToken(token)) {
                    return true;
                }
            }
        } catch (Exception ignore) {}
        return false;
    }
    public static boolean adminLoginChecker(String token) {
        if (!loginChecker(token)) return false;
        try {
            synchronized (FileLocks.user) {
                User user = new UserR().getUser(token.split("-")[0]);
                if (user.type.equals("admin")) return true;
            }
        } catch (Exception ignore){}
        return false;
    }
}
