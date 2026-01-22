package com.example.config;

import com.example.services.RagPipelineService;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Requires(notEnv = "test")
@Context
public class DBConfig {

    private static final Logger log = LoggerFactory.getLogger(DBConfig.class);
    private static final String FILE_NAME = "data/graalvm-graal-languages-demos.txt";



    private final RagPipelineService service;


    public DBConfig( RagPipelineService service ) {
        this.service = service;
    }



    @PostConstruct
    public void init() throws IOException {

        if (!service.checkDbInit())
            return;

        // Scrape documentation content from the GraalPy website
        service.loadDataFromWebSite(
                "https://www.graalvm.org/python/docs/",
                "docs-content docs-content--with-sidebar");


        // Load data from graalvm graal languages demos file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            if (inputStream == null) {
                log.error("File not found{}", FILE_NAME);
                return;
            }

            Path tempFile = Files.createTempFile("graalvm-graal-languages-demos", ".txt");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            service.loadDataFromFile(tempFile.toString());

        log.info("Document stored in vector store");

        service.CreateTextIndex();



    }
}