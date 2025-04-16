package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Inject;
import io.micronaut.views.View;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@io.micronaut.http.annotation.Controller
public class Controller {

    @Inject
    private Repository repository;
    private ExcelizeService service;

    public Controller(ExcelizeService service){
        this.service=service;
    }


    @Get // ③
    @View("index") // ④
    public void index() {

    }

    @Get(value="download", produces = MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<byte[]> downloadExcel() throws IOException {

        repository.save(new Book("Dragons","Anwar"));
        repository.save(new Book("Time","Anwar2"));
        repository.save(new Book("dragon lord","Anwar3"));

        List<Book> books= repository.findAll();
        System.out.println(books);



        Object[][] result = new Object[books.size() + 1][];
        result[0] = new Object[] { "ID", "Title", "Author" };
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            result[i + 1] = new Object[] {b.getId(), b.getTitle(), b.getAuthor() };
        }




        service.runExcelizeComplete(result);

        // Read the generated file
        Path excelFile = Paths.get("output.xlsx");
        byte[] fileContent = Files.readAllBytes(excelFile);
        // Return the file as a download
        return HttpResponse.ok(fileContent)
                .header("Content-Disposition", "attachment; filename=output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
    }



    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<String> uploadExcelFile(@Part("file") CompletedFileUpload file) {
        String filename = file.getFilename();

        if (!filename.endsWith(".xlsx")) {
            return HttpResponse.badRequest("Invalid file type. Only .xlsx files are accepted.");
        }

        try {
            byte[] fileBytes = file.getBytes();


            List<Book> books =  service.readExcelFromFile(fileBytes);
            for(Book book : books){
                if (book.getId() == null ||
                        repository.findById(book.getId()).isEmpty()) {
                    book.setId(null);
                    repository.save(book);
                } else {
                    repository.update(book);
                }


            }

            return HttpResponse.ok("the file was saved in the database successfully :" + filename);
        } catch (IOException e) {
            return HttpResponse.serverError("Error reading file: " + e.getMessage());
        }
    }


}
