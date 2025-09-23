/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.python.embedding.GraalPyResources;

public class App {

    public static void main(String[] args) {
        String input = args.length > 0 ? args[0] : "How can I check if my code runs on GraalPy?";
        try (Context context = GraalPyResources.contextBuilder()
                .allowEnvironmentAccess(EnvironmentAccess.INHERIT) // for OPENAI_API_KEY env var access
                .build()) {
            CreateResponseFunction createResponseFunction = context.eval("python",
                    // language=python
                    """
                            import os
                            from openai import OpenAI
                            
                            client = OpenAI(
                                # This is the default and can be omitted
                                api_key=os.environ.get("OPENAI_API_KEY"),
                            )
                            
                            def create_response(input):
                                return client.responses.create(
                                    model="gpt-4o",
                                    instructions="You are a coding assistant that talks like a pirate.",
                                    input=input,
                                )
                            
                            create_response
                            """).as(CreateResponseFunction.class);
            Response response = createResponseFunction.apply(input);
            System.out.println(response.output_text());
        } catch (PolyglotException e) {
            throw new RuntimeException("Failed to run Python code. Did you set the OPENAI_API_KEY environment variable?", e);
        }
    }

    @FunctionalInterface
    public interface CreateResponseFunction {
        Response apply(String choice);
    }

    public interface Response {
        String output_text();
    }
}
