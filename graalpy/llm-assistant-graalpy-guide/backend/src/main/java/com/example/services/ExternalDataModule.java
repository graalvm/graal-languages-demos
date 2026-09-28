package com.example.services;

public interface ExternalDataModule {

    Boolean is_graalpy_related(String url);

    Boolean add_url(String url);

    void add_new_text(String text);
}
