package si.f5.stsaria.didRecorder;

import java.security.MessageDigest;
import java.util.HexFormat;

public class Hash {
    public static String sha256Hex(String string) {
        try {
            return HexFormat.of().withLowerCase()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(string.getBytes()));
        } catch (Exception ignore) {
            return "";
        }
    }
}
