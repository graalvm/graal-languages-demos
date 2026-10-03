package com.example.controllers;

import com.example.services.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MicronautTest
class RagControllerUnitTest {

    @Inject @Client("/api/llm")
    HttpClient client;


    @Inject
    RagPipelineService service;


    @Test
    void testMocksAreInjected() {
        assertNotNull(service);
    }


    @Test
    void givenRelatedGraalPyQuestion_whenCallingGetAnswerEndpoint_thenReturnsExpectedAnswer() {
        //Arrange
        String query = "What is GraalPy?";
        Value dummyDocs   = mock(Value.class);

        when(service.hybridSearch(query, 4))
                .thenReturn(dummyDocs);
        when(service.generateAnswer(query, dummyDocs))
                .thenReturn("GraalPy is awesome");

        //Act
        HttpRequest<?> req = HttpRequest.POST("/answer", Map.of("query", query))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<String> rsp = client.toBlocking().exchange(req, String.class);
        //Assert
        assertEquals(HttpStatus.OK,rsp.getStatus());
        assertEquals("GraalPy is awesome", rsp.body());

        verify(service).hybridSearch(query, 4);
        verify(service).generateAnswer(query, dummyDocs);
    }


    @Test
    void givenValidUrl_whenAddUrlCalled_thenAddedSuccessfully() {
        String url = "https://graalpy.org/example";
        String expectedMessage = "The URL has been successfully added";

        when(service.addURL(url)).thenReturn(AddUrlResultType.SUCCESS);

        HttpRequest<?> req = HttpRequest.POST("/add-url", Map.of("url", url))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<Map> rsp = client.toBlocking().exchange(req, Map.class);

        assertEquals(HttpStatus.OK, rsp.getStatus());
        assertEquals(expectedMessage, rsp.body().get("message"));

        verify(service).addURL(url);
    }


    @Test
    void givenNonRelatedGraalPyURL_whenAddUrlCalled_thenReturnsBadRequest() {
        //Arrange
        String url = "https://graalpy.org/example";
        String expectedMessage = "This URL is either not related to GraalPy or cannot be scraped. Please use the text field instead.";

        when(service.addURL(url)).thenReturn(AddUrlResultType.INVALID_URL);

        //Act
        HttpRequest<?> req = HttpRequest.POST("/add-url", Map.of("url", url))
                .contentType(MediaType.APPLICATION_JSON);

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(req, Map.class);
        });

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(expectedMessage, ex.getMessage());

        verify(service).addURL(url);
    }

    @Test
    void givenExistingUrl_whenAddUrlCalled_thenReturnsExceptionError() {
        //Arrange
        String url = "https://github.com/oracle/graalpython";
        String expectedMessage = "The url content already exists";

        when(service.addURL(url)).thenReturn(AddUrlResultType.DUPLICATE);

        // Act
        HttpRequest<?> req = HttpRequest.POST("/add-url", Map.of("url", url))
                .contentType(MediaType.APPLICATION_JSON);

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(req, Map.class);
        });

        // Assert
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        Map<String, String> responseBody = ex.getResponse().getBody(Map.class).orElseThrow();

        assertEquals(expectedMessage, responseBody.get("message"));

        verify(service).addURL(url);
    }



    @Test
    void givenText_whenAddTextCalled_thenTextAddedSuccessfully() {
        //Arrange
        String text = "some interesting content about GraalPy";
        when(service.addText(text)).thenReturn("The text has been successfully added");

        //Act
        HttpRequest<?> req = HttpRequest.POST("/add-text", Map.of("text", text))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<Map> rsp = client.toBlocking().exchange(req, Map.class);

        //Assert
        assertEquals(HttpStatus.OK, rsp.getStatus());
        assertEquals("The text has been successfully added",
                rsp.body().get("message"));

        verify(service).addText(text);
    }

    @MockBean(RagPipelineService.class)
    public RagPipelineService mockService() {
        return mock(RagPipelineService.class);
    }


  
}
