package cn.misakanet.util;

import java.security.MessageDigest;

public class MDUtil {
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String digest(byte[] source, DigestType type) {
        try {
            // 获得MD摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance(type.digestDesc);
            // 使用指定的字节更新摘要
            mdInst.update(source);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public enum DigestType {

        MD5("MD5"), SHA("SHA"), SHA256("SHA-256"), SHA512("SHA-512");

        private final String digestDesc;

        DigestType(String digestDesc) {
            this.digestDesc = digestDesc;
        }

        public String getDigestDesc() {
            return digestDesc;
        }
    }
}
