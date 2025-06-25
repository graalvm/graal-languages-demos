package com.example;

import io.micronaut.core.io.ResourceResolver;
import org.openjdk.jmh.annotations.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 5)
@Fork(1)

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkRunner {

    ExcelizeService excelizeService;
    List<Book> sampleBooks;
    byte[] excelData;

    @Setup(Level.Trial)
    public void setup() throws IOException {

        ResourceResolver resolver = new ResourceResolver();
        ExcelizePool excelizePool= new ExcelizePool(resolver);
         this.excelizeService = new ExcelizeService(excelizePool);


        // Generate fake book list
        sampleBooks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            sampleBooks.add(new Book("Book " + i, "Author " + i));
        }

        // Generate Excel data once for readExcelFromFile
        excelData = Files.readAllBytes(Paths.get("src/main/resources/output.xlsx"));
    }

    @Benchmark
    public void benchmarkExcelExport() throws IOException {
        byte[] data = excelizeService.runExcelizeComplete(sampleBooks);
        //System.out.println(data.length);
    }

    @Benchmark
    public void benchmarkExcelImport() throws IOException {
        List<Book> books = excelizeService.readExcelFromFile(excelData);
        //System.out.println(books);
    }
}
