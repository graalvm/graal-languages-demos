package graalpy.micronaut.multithreaded;

import java.io.IOException;

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
    private final DataAnalysisModuleSingleContext das;
    private final DataAnalysisModuleMultiContext dam;

    public DataAnalysisController(DataAnalysisModuleSingleContext das, DataAnalysisModuleMultiContext dam) {
        this.das = das;
        this.dam = dam;
    }

    @Get
    @View("index")
    public void index() {
    }

    @ExecuteOn(TaskExecutors.IO)
    @Post(value = "/data_analysis_single", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    String analyzeCsvSingle(StreamingFileUpload file,
                    @Part("method") String analysisMethod,
                    @Part("column") String columnString) {
        return analyzeCsv(file, analysisMethod, columnString, das);
    }

    @ExecuteOn(TaskExecutors.IO)
    @Post(value = "/data_analysis_multi", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    String analyzeCsvMulti(StreamingFileUpload file,
                    @Part("method") String analysisMethod,
                    @Part("column") String columnString) {
        return analyzeCsv(file, analysisMethod, columnString, dam);
    }

    private String analyzeCsv(StreamingFileUpload file, String analysisMethod, String columnString, DataAnalysisModule da) {
        String csv;
        try {
            csv = new String(file.asInputStream().readAllBytes(), "UTF-8");
        } catch (IOException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Input file is not a UTF-8 CSV");
        }
        try {
            switch (analysisMethod) {
                case "mean":
                    return Double.toString(da.calculateMean(csv, Integer.parseInt(columnString)));
                case "median":
                    return Double.toString(da.calculateMedian(csv, Integer.parseInt(columnString)));
                case "describe":
                    return da.describe(csv);
                default:
                    throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Supported analysis methods are 'mean', 'median', 'describe'");
            }
        } catch (NumberFormatException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Column field must be an integer");
        } catch (PolyglotException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
