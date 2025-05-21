/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.core.io.ResourceResolver;
import org.graalvm.polyglot.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



@io.micronaut.context.annotation.Context
public class ExcelizeService {

    private final ResourceResolver resourceResolver;
    private final ExcelizePool excelizePool;

    public ExcelizeService(ResourceResolver resourceResolver,ExcelizePool excelizePool) {
        this.resourceResolver = resourceResolver;
        this.excelizePool = excelizePool;
    }


    public void runExcelizeComplete(Object[][] array) throws IOException {
                Context context = excelizePool.getContext();
                Value x = context.getBindings("js").getMember("generateExcel");
                Value jsArray = context.eval("js", "[]");
                for (Object[] row : array) {
                    Value jsRow = context.eval("js", "[]");
                    for (Object cell : row) {
                        jsRow.setArrayElement(jsRow.getArraySize(), cell == null ? Value.asValue((Object) null) : cell);
                    }
                    jsArray.setArrayElement(jsArray.getArraySize(), jsRow);
                }

                x.execute(jsArray);
                // Save output Excel file
                Value buffer = context.getPolyglotBindings().getMember("excelBuffer");
                if (buffer != null && buffer.hasArrayElements()) {
                    int length = (int) buffer.getArraySize();
                    byte[] fileBytes = new byte[length];
                    for (int j = 0; j < length; j++) {
                        fileBytes[j] = (byte) buffer.getArrayElement(j).asInt();
                    }
                    Files.write(Paths.get("src/main/resources/output.xlsx"), fileBytes);
                    System.out.println("Excel file saved as output.xlsx");
                } else {
                    System.err.println("No buffer exported from JS.");
                }
                excelizePool.release(context);

            }

    public List<Book> readExcelFromFile(byte[] excelBytes) throws IOException {
        Context context = excelizePool.getContext();

            byte[] fileBytes = resourceResolver.getResourceAsStream("classpath:output.xlsx").get().readAllBytes();
            // Convert file bytes to a JS array
            Value jsArray = context.eval("js", "[]");
            for (byte b : fileBytes) {
                // Ensure the byte is passed as a number (0..255)
                jsArray.setArrayElement(jsArray.getArraySize(), b & 0xFF);
            }
            // Expose the file bytes to the JS context via polyglot binding "excelFile"
            context.getPolyglotBindings().putMember("excelFile", jsArray);

            context.getBindings("js").putMember("excelFileBytes", excelBytes);


            Value readFunc = context.getBindings("js").getMember("readExcel");readFunc.execute();
            Value bufferArray = context.getPolyglotBindings().getMember("resultArray");



            List<Book> books = new ArrayList<>();


            if (bufferArray.hasArrayElements()) {
                for (int i = 1; i < bufferArray.getArraySize(); i++) {
                    Value row = bufferArray.getArrayElement(i);
                    if (row.hasArrayElements()) {

                        String id = row.getArrayElement(0).asString();
                        String author = row.getArrayElement(1).asString();
                        String title = row.getArrayElement(2).asString();
                        System.out.println(id);
                        // Create a new Book object and add it to the list
                        books.add(new Book(id,author, title));
                    }
                }
            }
            excelizePool.release(context);
            return books;
        }
      }

