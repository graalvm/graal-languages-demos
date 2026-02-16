package org.example;


import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;

public class App {

    public static void main(String[] args) {

            try (Context context = GraalPyResources.contextBuilder()
                    .allowAllAccess(true) // ①
                    .option("python.WarnExperimentalFeatures", "false") // ②
                    .build()) { // ③

                Value value = context.eval("python", "import convert_file; convert_file");
                ConvertFile convertFile = value.as(ConvertFile.class);
                String text = convertFile.convert("src/main/resources/test.pdf");
                System.out.println(text);

            }
        }


}