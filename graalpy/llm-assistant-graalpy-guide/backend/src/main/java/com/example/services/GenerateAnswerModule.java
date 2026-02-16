package com.example.services;

import org.graalvm.polyglot.Value;

public interface GenerateAnswerModule {

    String process_question(String question, Value documents);
}
