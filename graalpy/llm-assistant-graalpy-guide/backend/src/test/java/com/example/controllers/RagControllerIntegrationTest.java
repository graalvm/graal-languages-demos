package com.example.controllers;

import com.example.config.GraalPyContext;
import com.example.services.RagPipelineService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import java.util.Map;

import static com.example.services.RagPipelineService.TABLE_NAME;
import static org.junit.jupiter.api.Assertions.*;



@MicronautTest(environments = "test")
public class RagControllerIntegrationTest {

    @Inject @Client("/api/llm")
    HttpClient client;

    @Inject
    private GraalPyContext graalPyContext;

    @Inject
    private RagPipelineService service;





    @BeforeEach
     void setUp() {
        service.addText("GraalPy is a high-performance implementation of the Python language for the JVM built on GraalVM. "+
        "GraalPy is a Python 3.11 compliant runtime. It has first-class support for embedding in Java and can turn Python applications into fast, standalone binaries. "+
        "GraalPy is ready for production running pure Python code and has experimental support for many popular native extension modules.");

        service.addText("Installing Python packages with Maven is a bit more involved and requires a bit of understanding of how Python applications are usually packaged and distributed."+
                "Python libraries and packages can be installed system-wide or per user, but that is rarely desirable if we want to distribute self-contained applications and avoid conflicts with the rest of the system."+
                "For that reason the Python community recommends using the venv Python module to create a virtual environment for your projects. The venv module is part of the Python 3 standard library."+
                "Users of older Python 2 versions may remember a virtualenv package that served the same purpose, but which was external to the Python standard library.");

        service.CreateTextIndex();
    }

    @Test
    void givenRelatedGraalPyQuestion_whenCallingGetAnswerEndpoint_thenReturnsExpectedAnswer() {

        //Arrange
        String query = "What is GraalPy?"; // ①

        //Act
        HttpRequest<?> req = HttpRequest.POST("/answer", Map.of("query", query))
                .contentType(MediaType.APPLICATION_JSON); // ②
        HttpResponse<String> rsp = client.toBlocking().exchange(req, String.class);

        //Assert
        assertEquals(HttpStatus.OK,rsp.getStatus()); // ③
        assert(rsp.body().toLowerCase().contains("graalpy"));
        assert (rsp.body().toLowerCase().contains("embedding"));
        assert (rsp.body().toLowerCase().contains("language"));

    }

    @Test
    void givenUnrelatedGraalPyQuestion_whenCallingGetAnswerEndpoint_thenReturnsGraalPySpecializationMessage(){

        //Arrange
        String query = "What is Kafka Streams?";
        String expectedAnswer = "I specialize in GraalPy. Please provide a GraalPy-related question.";

        //Act
        HttpRequest<?> req = HttpRequest.POST("/answer", Map.of("query", query))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<String> rsp = client.toBlocking().exchange(req, String.class);
        //Assert
        assertEquals(HttpStatus.OK,rsp.getStatus());
        assertEquals(expectedAnswer, rsp.body());


    }


    @Test
    void givenValidUrl_whenAddUrlCalled_thenAddedSuccessfully() {
        //Arrange
        String url = "https://www.graalvm.org/python/";
        String expectedMessage = "The URL has been successfully added";

        //Act
        HttpRequest<?> req = HttpRequest.POST("/add-url", Map.of("url", url))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<Map> rsp = client.toBlocking().exchange(req, Map.class);


        //Assert
        assertEquals(HttpStatus.OK, rsp.getStatus());
        assertEquals(expectedMessage, rsp.body().get("message"));

    }

    @Test
    void givenNonGraalPyUrl_whenAddUrlCalled_thenReturnsExceptionError() {
        //Arrange
        String url = "https://example.com";
        String expectedMessage = "This URL is either not related to GraalPy or cannot be scraped. Please use the text field instead.";

        //Act
        HttpRequest<?> req = HttpRequest.POST("/add-url", Map.of("url", url))
                .contentType(MediaType.APPLICATION_JSON);

        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(req, Map.class);
        });

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(expectedMessage, ex.getMessage());


    }

    @Test
    void givenExistingUrl_whenAddUrlCalled_thenReturnsExceptionError() {
        //Arrange
        String url = "https://github.com/oracle/graalpython";
        String expectedMessage = "The url content already exists";

        //language=Python
        graalPyContext.getContext().eval("python", """
                    from vector_store_manager import VectorStoreManager
                    vector_store_manager = VectorStoreManager("%s")
                    cursor = vector_store_manager.get_cursor()
                    try:
                          metadata = '{"source": "https://github.com/oracle/graalpython"}'
                          cursor.execute(
                            "INSERT INTO %s (metadata) VALUES (:metadata)",
                             {"metadata": metadata}
                          )
                          cursor.execute("COMMIT")
                    except:
                        pass
                    """.formatted(TABLE_NAME, TABLE_NAME) );

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


    }


    @Test
    void givenText_whenAddTextCalled_thenTextAddedSuccessfully() {
        //Arrange
        String text = "GraalPy is a high-performance implementation of the Python language for the JVM built on GraalVM.";
        String expectedMessage = "The text has been successfully added";
        //Act
        HttpRequest<?> req = HttpRequest.POST("/add-text", Map.of("text", text))
                .contentType(MediaType.APPLICATION_JSON);
        HttpResponse<Map> rsp = client.toBlocking().exchange(req, Map.class);

        //Assert
        assertEquals(HttpStatus.OK, rsp.getStatus());
        assertEquals(expectedMessage, rsp.body().get("message"));

    }


    @AfterEach
    void tearDown() {

            //language=Python
            String pythonCode = """
                from vector_store_manager import VectorStoreManager
                vector_store_manager = VectorStoreManager("%s")
                cursor = vector_store_manager.get_cursor()
                try:
                    cursor.execute("DROP TABLE %s CASCADE CONSTRAINTS")
                except:
                    pass
                """.formatted(TABLE_NAME, TABLE_NAME);


            graalPyContext.getContext().eval("python", pythonCode);


    }

}
