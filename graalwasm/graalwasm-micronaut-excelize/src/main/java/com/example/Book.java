/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;




import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;


@Entity
@Serdeable

public class Book {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Title;
    private String author;


    public Book() {

    }
    public Book(String id,String title, String author) {

        this.id= Long.valueOf(id);
        this.Title = title;
        this.author = author;
    }
    public Book(String title, String author) {

        Title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return author;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
