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

@Controller
public class DataAnalysisController {
    private final PythonPool pool;

    public DataAnalysisController(PythonPool pool) {
        this.pool = pool;
    }

    @Get
    @View("index")
    public void index() {
    }

    @ExecuteOn(TaskExecutors.IO)
    @Post(value = "/data_analysis", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    String analyzeCsvMulti(StreamingFileUpload file,
                    @Part("poolSize") String poolSizeString) {
        String csv;
        try {
            pool.setPoolSize(Integer.parseInt(poolSizeString));
        } catch (IllegalArgumentException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Pool size must be a positive integer");
        }
        try {
            csv = new String(file.asInputStream().readAllBytes(), "UTF-8");
        } catch (IOException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Input file is not a UTF-8 CSV");
        }
        try {
            return pool.submit(() -> {
                return pool.eval("import data_analysis; data_analysis").invokeMember("mean", csv);
            }).get().toString();
        } catch (PolyglotException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (InterruptedException e) {
            throw new HttpStatusException(HttpStatus.GATEWAY_TIMEOUT, e.getMessage());
        } catch (ExecutionException e) {
            throw new HttpStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}
