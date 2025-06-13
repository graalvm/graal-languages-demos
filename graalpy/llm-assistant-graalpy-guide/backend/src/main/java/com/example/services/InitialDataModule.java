package com.example.services;

public interface InitialDataModule {

    Boolean check_db_init();

    void load_data_from_url_process(String url, String className);

    void load_data_from_file_process(String fileName);
    void create_text_index();
}
