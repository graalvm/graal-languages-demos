package graalpy.micronaut.multithreaded;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;

@io.micronaut.context.annotation.Context // ①
public class PythonPool {
    private final Engine engine;
    private final BlockingDeque<Context> contexts;
    private int size;

    public PythonPool() {
        engine = Engine.create(); // ②
        contexts = new LinkedBlockingDeque<>(); // ③
        size = 0;
        setPoolSize(2); // ④
    }

    public synchronized void setPoolSize(int newSize) { // ①
        if (newSize <= 0) {
            throw new IllegalArgumentException();
        }
        while (size > newSize) {
            try {
                contexts.takeLast();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            size--;
        }
        while (size < newSize) {
            contexts.addLast(createContext(engine));
            size++;
        }
    }

    private static Context createContext(Engine engine) {
        var resourcesDir = Path.of(System.getProperty("user.dir"), "graalpy.resources");
        if (!resourcesDir.toFile().isDirectory()) { // ②
            var fs = VirtualFileSystem.create();
            try {
                GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        var context = GraalPyResources.contextBuilder(resourcesDir)
            .engine(engine)
            .allowNativeAccess(true) // ③
            .allowCreateProcess(true) // ④
            .allowExperimentalOptions(true)
            .option("python.IsolateNativeModules", "true") // ⑤
            .build();
        context.initialize("python");
        return context;
    }

    public <T> T execute(Function<Context, T> action) {
        Context c;
        try {
            c = contexts.takeFirst(); // ①
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        contexts.addLast(c);
        T result = action.apply(c);
        if (!(result instanceof Number || result instanceof String)) { // ②
            throw new IllegalStateException("Instances must not leak out of PythonPool#execute. " +
                            "Convert the value to a java.lang.Number or a java.lang.String, before returning it.");
        }
        return result;
    }
}
