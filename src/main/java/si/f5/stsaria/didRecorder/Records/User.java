package si.f5.stsaria.didRecorder.Records;

public class User {
    public final String id;
    public final String name;
    public final String sha256Pass;
    public final long makeUnixTime;
    public final String type;

    public final String csvString;

    public User(String id, String name, String sha256Pass, long makeUnixTime, String type){
        this.id = id;
        this.name = name;
        this.sha256Pass = sha256Pass;
        this.makeUnixTime = makeUnixTime;
        this.type = type;

        this.csvString = id+","+name+","+sha256Pass+","+makeUnixTime+","+type;
    }
}
