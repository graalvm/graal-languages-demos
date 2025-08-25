package com.example.controllers;

import com.example.services.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.cors.CrossOrigin;
import jakarta.inject.Inject;
import org.graalvm.polyglot.Value;
import java.util.Map;

@CrossOrigin("*")
@Controller("/api/llm")
public class RagController {


    @Inject
    private RagPipelineService service;

    @Post(value = "/answer")
    public String getAnswer(@Body Map<String, String> body) { // ①
        String query = body.get("query");
        Value retrievedDocs = service.hybridSearch(query, 4); // ②
        return service.generateAnswer(query, retrievedDocs); // ③
    }



    @Post("/add-url")
    public HttpResponse<Map<String, String>> addUrl(@Body Map<String, String> body) {
        String url = body.get("url");

        AddUrlResultType result = service.addURL(url);

        return switch (result) {
            case INVALID_URL -> HttpResponse.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "This URL is either not related to GraalPy or cannot be scraped. Please use the text field instead."));
            case DUPLICATE -> HttpResponse.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "The url content already exists"));
            case SUCCESS -> HttpResponse.ok(Map.of("message", "The URL has been successfully added"));
        };
    }

    @Post("/add-text")
    public HttpResponse<Map<String, String>> addText(@Body Map<String, String> body) {
        String text = body.get("text");

        String message = service.addText(text);

        return HttpResponse.ok(Map.of("message", message));


    }

}
