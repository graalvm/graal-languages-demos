package graalpy.micronaut.multithreaded;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.graalvm.polyglot.PolyglotException;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;

@Controller // ①
public class DataAnalysisController {
    private final PythonPool pool;

    public DataAnalysisController(PythonPool pool) { // ②
        this.pool = pool;
    }

    @Get // ③
    @View("index") // ④
    public void index() {
    }

    @Post(value = "/data_analysis", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN) // ⑤
    @ExecuteOn(TaskExecutors.IO) // ⑥
    String analyzeCsvMulti(StreamingFileUpload file,
                    @Part("poolSize") String poolSizeString) {
        try {
            pool.setPoolSize(Integer.parseInt(poolSizeString));
            String csv = new String(file.asInputStream().readAllBytes(), "UTF-8");
            return pool.submit(() -> { // ⑦
                return pool.eval("import data_analysis; data_analysis").invokeMember("mean", csv);
            }).get().toString();
        } catch (Exception e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
