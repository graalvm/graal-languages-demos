package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;

import java.io.File;
import java.util.List;

public class App extends Application {

    private Context context;
    private static ConvertFiles convertFiles;
    private File selectedFile;

    @Override
    public void start(Stage stage) {
        context = GraalPyResources.contextBuilder()
                .allowAllAccess(true) // ①
                .option("python.WarnExperimentalFeatures", "false") // ②
                .build(); // ③

        setupWindow(stage);
    }

    private void setupWindow(Stage stage) {

        Label fileLabel = new Label("Drag file here or click to upload");
        fileLabel.setStyle("-fx-border: 2px dashed #ccc; -fx-padding: 20; -fx-alignment: center;");

        HBox fileSection = new HBox(10);
        fileSection.getChildren().add(fileLabel);
        fileSection.setPadding(new Insets(10));

        Button convertButton = new Button("Convert to Text");
        convertButton.setDisable(true);
        convertButton.setPrefWidth(120);

        fileLabel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        fileLabel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (!files.isEmpty()) {
                    selectedFile = files.get(0);
                    fileLabel.setText("Selected: " + selectedFile.getName());
                    convertButton.setDisable(false);
                }
            }
            event.consume();
        });

        fileLabel.setOnMouseClicked(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.pptx", "*.txt"),
                    new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedFile = file;
                fileLabel.setText("Selected: " + file.getName());
                convertButton.setDisable(false);
            }
        });
        convertButton.setDisable(true);
        convertButton.setPrefWidth(120);

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPromptText("Converted text will appear here...");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        convertButton.setOnAction(e -> {
            if (selectedFile != null) {
                convertFileWithGraalPy(selectedFile, textArea);
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(fileSection, convertButton, textArea);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.setTitle("MarkitDown Demo");
        stage.show();
    }

    private void convertFileWithGraalPy(File file, TextArea textArea) {
        try {
            textArea.setText("Converting...");
            Value value = context.eval("python", "import convert_files; convert_files");
            convertFiles = value.as(ConvertFiles.class);
            String text = convertFiles.convert(file.getAbsolutePath());
            textArea.setText(text);
        } catch (Exception e) {
            textArea.setText("Error: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        context.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}