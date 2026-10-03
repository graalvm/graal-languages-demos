package com.example;

import org.apache.arrow.vector.Float8Vector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class DownloadUtils {

    public static void downloadAndStore(String url, int columnIndex, Float8Vector vectorToStore) {
        try {
            var httpConnection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                    String line;
                    int index = 0;
                    while ((line = reader.readLine()) != null) {
                        var values = line.split(",");
                        double value = Double.parseDouble(values[columnIndex]);
                        vectorToStore.setSafe(index, value);
                        index++;
                    }
                    vectorToStore.setValueCount(index);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
