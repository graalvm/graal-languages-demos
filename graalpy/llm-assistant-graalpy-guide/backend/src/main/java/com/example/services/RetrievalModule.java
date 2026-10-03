package com.example.services;

import org.graalvm.polyglot.Value;

public interface RetrievalModule {

    Value hybrid_search(String question, int numResults);
}
