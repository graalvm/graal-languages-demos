/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



@io.micronaut.context.annotation.Context
public class ExcelizeService {


    private final ExcelizePool excelizePool;

    public ExcelizeService(ExcelizePool excelizePool) {
        this.excelizePool = excelizePool;
    }
        public byte[] runExcelizeComplete(List<Book> books) throws IOException {
        Context context = excelizePool.getContext();
        try{
                Value function = context.getBindings("js").getMember("generateExcel");
            Value jsArray = context.eval("js", "[]");

            // Add headers as the first row
            Value headerRow = context.eval("js", "[]");
            headerRow.setArrayElement(0, "ID");
            headerRow.setArrayElement(1, "Title");
            headerRow.setArrayElement(2, "Author");
            jsArray.setArrayElement(0, headerRow);

            // Add book data rows
            for (int i = 0; i < books.size(); i++) {
                Book b = books.get(i);
                Value jsRow = context.eval("js", "[]");
                jsRow.setArrayElement(0, b.getId() == null ? "" : b.getId());
                jsRow.setArrayElement(1, b.getTitle());
                jsRow.setArrayElement(2, b.getAuthor());
                jsArray.setArrayElement(i + 1, jsRow);
            }


            function.execute(jsArray);
                // Save output Excel file
                Value buffer = context.getPolyglotBindings().getMember("excelBuffer");
                if (buffer != null && buffer.hasArrayElements()) {
                    int length = (int) buffer.getArraySize();
                    byte[] fileBytes = new byte[length];
                    for (int j = 0; j < length; j++) {
                        fileBytes[j] = (byte) buffer.getArrayElement(j).asInt();
                    }
                    return fileBytes;
                } else {
                    System.err.println("No buffer exported from JS.");
                }
        }finally {
            excelizePool.release(context);
        }
        return null;
    }

    public List<Book> readExcelFromFile(byte[] excelBytes) throws IOException {

        Context context = excelizePool.getContext();
        List<Book> books = new ArrayList<>();
        try {

            Value readFunc = context.getBindings("js").getMember("readExcel");
            readFunc.execute(excelBytes).invokeMember("then",(ProxyExecutable) result -> {
                Value bufferArray = result[0];
                if (bufferArray.hasArrayElements()) {
                    for (int i = 1; i < bufferArray.getArraySize(); i++) {
                        Value row = bufferArray.getArrayElement(i);
                        if (row.hasArrayElements()) {

                            String id = row.getArrayElement(0).asString();
                            String author = row.getArrayElement(1).asString();
                            String title = row.getArrayElement(2).asString();
                            // Create a new Book object and add it to the list
                            books.add(new Book(id, author, title));
                        }
                    }
                }
                return null;

            });

        }finally {
            excelizePool.release(context);
        }
            return books;
        }
      }

