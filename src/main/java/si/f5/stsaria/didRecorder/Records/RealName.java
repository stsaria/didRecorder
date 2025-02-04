package si.f5.stsaria.didRecorder.Records;

public class RealName {
    public final User user;
    public final String name;
    public final long makeUnixTime;

    public final String csvString;

    public RealName(User user, String name, long makeUnixTime){
        this.user = user;
        this.name = name;
        this.makeUnixTime = makeUnixTime;

        this.csvString = user.id+","+name+","+makeUnixTime;
    }
}
