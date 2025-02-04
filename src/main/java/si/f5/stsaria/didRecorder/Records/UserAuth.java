package si.f5.stsaria.didRecorder.Records;

import si.f5.stsaria.didRecorder.Hash;

public class UserAuth {
    public final User user;
    public final String auth;
    public final long makeUnixTime;
    public final long limitUnixTime;

    public final String csvString;

    public UserAuth(User user, String auth, long makeUnixTime, long limitUnixTime) {
        this.user = user;
        this.auth = auth;
        this.makeUnixTime = makeUnixTime;
        this.limitUnixTime = limitUnixTime;

        this.csvString = user.id+","+Hash.sha256Hex(auth)+","+makeUnixTime+","+limitUnixTime;
    }
}
