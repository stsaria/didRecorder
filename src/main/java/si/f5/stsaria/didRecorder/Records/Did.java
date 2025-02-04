package si.f5.stsaria.didRecorder.Records;

public class Did {
    public final User user;
    public final int type;
    public final long makeUnixTime;
    public final String contentString;
    public final long contentUnixTime;

    public final String csvString;

    public Did(User user, int type, long makeUnixTime, String content){
        this.user = user;
        this.type = type;
        this.makeUnixTime = makeUnixTime;
        this.contentString = content;

        this.contentUnixTime = 0;

        this.csvString = user.id+","+type+","+makeUnixTime+","+content;
    }
    public Did(User user, int type, long makeUnixTime, long content){
        this.user = user;
        this.type = type;
        this.makeUnixTime = makeUnixTime;
        this.contentUnixTime = content;

        this.contentString = "";

        this.csvString = user.id+","+type+","+makeUnixTime+","+content;
    }
}
