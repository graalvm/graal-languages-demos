package com.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class MicronautdemoTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testDownloadExcelEndpoint() {
        byte[] response = client.toBlocking().retrieve(HttpRequest.GET("/download"), byte[].class);

        assertNotNull(response);
        assertTrue(response.length > 0, "Excel file should not be empty");
    }

    @Test
    void testUploadExcelEndpoint() throws IOException {
        Path filePath = Paths.get("src/main/resources/output.xlsx");
        InputStream is = Files.newInputStream(filePath);
        assertNotNull(is, "output.xlsx should exist in src/test/resources");

        MultipartBody body = MultipartBody.builder()
                .addPart("file", "output.xlsx", MediaType.APPLICATION_OCTET_STREAM_TYPE, is.readAllBytes())
                .build();

        String response = client.toBlocking().retrieve(HttpRequest.POST("/upload", body)
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE), String.class);

        assertTrue(response.contains("successfully"), "Upload should respond with success message");
    }
}
