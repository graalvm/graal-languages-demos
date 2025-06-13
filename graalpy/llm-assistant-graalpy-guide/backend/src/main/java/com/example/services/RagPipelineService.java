package com.example.services;

import com.example.config.GraalPyContext;
import jakarta.inject.Singleton;
import org.graalvm.polyglot.Value;

import static com.example.config.GraalPyContext.PYTHON;


@Singleton
public class RagPipelineService {


    private final GenerateAnswerModule generateAnswerModule;
    private final InitialDataModule initialDataModule;
    private final RetrievalModule retrievalModule;
    private final ExternalDataModule externalDataModule;
    public static final String TABLE_NAME = "VECTOR_STORE";

    public RagPipelineService(GraalPyContext graalPyContext)  {


        graalPyContext.getContext().eval(PYTHON, "import generation, prepare_initial_data, external_data_processing, retrieval"); // ①
        Value generation_module = graalPyContext.getContext().getBindings(PYTHON).getMember("generation"); // ②
        Value retrieval_module = graalPyContext.getContext().getBindings(PYTHON).getMember("retrieval");
        Value initial_data_module = graalPyContext.getContext().getBindings(PYTHON).getMember("prepare_initial_data");
        Value external_data_module = graalPyContext.getContext().getBindings(PYTHON).getMember("external_data_processing");

        Value generateAnswerClass = generation_module.getMember("GenerateAnswer"); // ③
        Value retrievalClass = retrieval_module.getMember("Retrieval");
        Value initialDataClass = initial_data_module.getMember("InitialData");
        Value externalDataClass = external_data_module.getMember("ExternalData");


        generateAnswerModule = generateAnswerClass.newInstance().as(GenerateAnswerModule.class); // ④
        retrievalModule = retrievalClass.newInstance(TABLE_NAME).as(RetrievalModule.class);
        initialDataModule = initialDataClass.newInstance(TABLE_NAME).as(InitialDataModule.class);
        externalDataModule = externalDataClass.newInstance(TABLE_NAME).as(ExternalDataModule.class);

    }

    public String generateAnswer(String question, Value retrievedDocuments) {
        return generateAnswerModule.process_question(question, retrievedDocuments); // ⑤
    }

    public Value hybridSearch(String question, int numResults) {
        return retrievalModule.hybrid_search(question, numResults);
    }

    public AddUrlResultType addURL(String url){
        if(!externalDataModule.is_graalpy_related(url)){
            return AddUrlResultType.INVALID_URL;
        }
        if(!externalDataModule.add_url(url)){
            return AddUrlResultType.DUPLICATE;
        }
        return AddUrlResultType.SUCCESS;
    }

    public String addText(String text){
        externalDataModule.add_new_text(text);
        return "The text has been successfully added";
    }

    public Boolean checkDbInit(){
        return initialDataModule.check_db_init();
    }

    public void loadDataFromWebSite(String url, String className){
        initialDataModule.load_data_from_url_process(url, className);
    }
    public void loadDataFromFile(String fileName){
        initialDataModule.load_data_from_file_process(fileName);

    }

    public void CreateTextIndex(){
        initialDataModule.create_text_index();
    }



}
