package si.f5.stsaria.didRecorder.checker;

import si.f5.stsaria.didRecorder.Users;

public class Login {
    public static boolean loginChecker(String token) {
        boolean loggedIn = false;
        try {
            synchronized (Users.lock) {
                if (new Users().authForToken(token)) {
                    loggedIn = true;
                }
            }
        } catch (Exception ignore) {}
        return loggedIn;
    }
    public static boolean adminLoginChecker(String token) {
        if (!loginChecker(token)) return false;
        try {
            synchronized (Users.lock) {
                if (!new Users().isAdmin(token.split("\\.")[0])) return false;
            }
        } catch (Exception ignore){}
        return true;
    }
}
