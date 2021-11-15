package cn.misakanet.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static String getExt(File file) {
        return getExt(file.getPath());
    }

    public static String getExt(String path) {
        int extIndex = path.lastIndexOf(".");
        if (extIndex == -1) {
            return null;
        }

        return path.substring(extIndex);
    }

    public static void save(File file, String content) throws IOException {
        try (var fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String load(File file) throws IOException {
        try (var fis = new FileInputStream(file);
             var fc = fis.getChannel()) {

            var decoder = StandardCharsets.UTF_8.newDecoder();

            StringBuilder sb = new StringBuilder();
            ByteBuffer buf = ByteBuffer.allocate(512);
            CharBuffer cBuf = CharBuffer.allocate(512);

            char[] tmp = null;
            byte[] remainByte = null;
            int leftNum = 0;
            int bytesRead = fc.read(buf);
            while (bytesRead != -1) {
                buf.flip();
                decoder.decode(buf, cBuf, true);
                cBuf.flip();

                remainByte = null;
                leftNum = buf.limit() - buf.position();

                if (leftNum > 0) { // 记录未转换完的字节
                    remainByte = new byte[leftNum];
                    buf.get(remainByte, 0, leftNum);
                }

                tmp = new char[cBuf.length()];

                while (cBuf.hasRemaining()) {
                    cBuf.get(tmp);
                    sb.append(tmp);
                }

                buf.clear();
                cBuf.clear();

                if (remainByte != null) {
                    buf.put(remainByte);
                }

                bytesRead = fc.read(buf);
            }

            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
