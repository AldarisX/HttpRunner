package cn.misakanet.encoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public interface Encoder {
    static String base64Encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
    }

    static byte[] base64Decode(String base64Code) {
        return Base64.getDecoder().decode(base64Code.getBytes(StandardCharsets.UTF_8));
    }

    String encode(String data);

    String encode(String data, String key);

    String decode(String data);

    String decode(String data, String key);
}
