package com.example;

import java.net.HttpURLConnection;
import java.net.URI;

public class DownloadUtils {
    public static String download(String url) {
        try {
            var httpConnection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            int code = httpConnection.getResponseCode();
            switch (code) {
                case HttpURLConnection.HTTP_OK:
                    return new String(httpConnection.getInputStream().readAllBytes(), "UTF-8");
                default:
                    throw new RuntimeException(Integer.toString(code));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
