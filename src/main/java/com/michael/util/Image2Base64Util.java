package com.michael.util;

import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 图片转base64
 */
public class Image2Base64Util {
    public static String netImageToBase64(String netImagePath) {
        try {
            // 创建URL
            URL url = new URL(netImagePath);
            // 创建链接
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            InputStream is = conn.getInputStream();
            return ioImageToBase64(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String ioImageToBase64(InputStream is) {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            final byte[] by = new byte[1024];
            // 将内容读取内存中
            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }
            // 关闭流
            is.close();
            return byteImageToBase64(data.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String byteImageToBase64(byte[] by) {
        if(by != null) {
            BASE64Encoder encoder = new BASE64Encoder();
            String strNetImageToBase64 = encoder.encode(by);
            return strNetImageToBase64.replace("\n", "").replace("\r", "");
        } else {
            return "";
        }
    }
}
