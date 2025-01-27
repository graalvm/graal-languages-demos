/*
 * Copyright (c) 2011, 2024, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.io.IOAccess;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class App extends Application {
    private Context context;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void stop() throws Exception {
        context.close();
        super.stop();
    }

    @Override
    public void start(Stage stage) {
        context = Context.newBuilder("python")
            .allowIO(IOAccess.newBuilder() // ①
                            .fileSystem(FileSystem.newReadOnlyFileSystem(FileSystem.newDefaultFileSystem()))
                            .build())
            .allowPolyglotAccess(PolyglotAccess.newBuilder() // ②
                            .allowBindingsAccess("python")
                            .build())
            // Above are all the options we need to run the app
            // The options below  allow us to debug the Python code while running in Java
            .option("dap", "localhost:4711")
            .option("dap.Suspend", "false")
            .build();

        setupWindow(stage);
    }

    private void setupWindow(Stage stage) {
        stage.setTitle("Similarity score");

        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 200);

        final Text target = new Text(200, 100, "DROP FILES HERE");
        target.setTextAlignment(TextAlignment.CENTER);
        resetTargetColor(target);
        target.setScaleX(2.0);
        target.setScaleY(2.0);
        StackPane.setMargin(target, new Insets(10, 10, 10, 10));

        target.setOnDragOver((event) -> {
            if (event.getGestureSource() != target && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        }
        );

        target.setOnDragEntered((event) -> {
            if (event.getGestureSource() != target && event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 2) {
                target.setFill(Color.GREEN);
            } else {
                target.setText("Drop 2 files to compare.");
            }
            event.consume();
        }
        );

        target.setOnDragExited((event) -> {
            resetTargetColor(target);
            event.consume();
        });

        setupFileComparison(target);

        root.getChildren().add(target);
        stage.setScene(scene);

        stage.show();

        if (getParameters().getRaw().contains("CI")) {
            stage.close();
        }
    }

    private static void resetTargetColor(final Text target) {
        target.setFill(Color.LIGHTGRAY);
    }

    private void setupFileComparison(final Text target) {
        try {
            context.eval(Source.newBuilder("python", App.class.getResource("/compare_files.py")).build());  // ①
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Value compareFiles = context.getBindings("python").getMember("compare_files"); // ②

        target.setOnDragDropped((event) -> {
            boolean success = false;
            List<File> files;
            if ((files = event.getDragboard().getFiles()) != null && files.size() == 2) {
                try {
                    File file0 = files.get(0), file1 = files.get(1);
                    double result = compareFiles.execute(file0.getAbsolutePath(), file1.getAbsolutePath()).asDouble(); // ③
                    target.setText(String.format("%s = %f x %s", file0.getName(), result, file1.getName()));
                    success = true;
                } catch (RuntimeException e) {
                    target.setText(e.getMessage());
                }
            }
            resetTargetColor(target);
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
