package graalpy.micronaut.multithreaded;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.utils.GraalPyResources;
import org.graalvm.python.embedding.utils.VirtualFileSystem;

@io.micronaut.context.annotation.Context
public class PythonPool extends AbstractExecutorService {
    private final Engine engine;
    private final ThreadLocal<Context> thisContext;
    private final BlockingQueue<Context> contexts;
    private final ExecutorService threadPool;
    private final int size;

    private static Context createContext(Engine engine) {
        var resourcesDir = Path.of(System.getProperty("user.dir"), "graalpy.resources");
        var rf = resourcesDir.toFile();
        synchronized (PythonPool.class) {
            if (!rf.isDirectory() || rf.lastModified() / 1000 < ProcessHandle.current().info().startInstant().get().getEpochSecond()) {
                var fs = VirtualFileSystem.create();
                try {
                    GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        var context = GraalPyResources.contextBuilder(resourcesDir)
            .engine(engine)
            .allowExperimentalOptions(true)
            .option("python.PythonHome", "")
            .option("python.WarnExperimentalFeatures", "false")
            .option("python.IsolateNativeModules", "true")
            .allowNativeAccess(true)
            .allowCreateProcess(true)
            .build();
        context.initialize("python");
        return context;
    }

    public Value eval(String code) {
        var c = thisContext.get();
        if (c == null) {
            throw new IllegalStateException("PythonPool#eval can only be called from inside a submitted task");
        }
        return c.eval("python", code);
    }

    public PythonPool() {
        this(5);
    }

    public int getPoolSize() {
        return size;
    }

    private PythonPool(int nContexts) {
        size = nContexts;
        engine = Engine.create();
        contexts = new LinkedBlockingQueue<>();
        thisContext = new ThreadLocal<>();
        threadPool = Executors.newFixedThreadPool(nContexts, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(() -> {
                    var c = createContext(engine);
                    contexts.add(c);
                    thisContext.set(c);
                    r.run();
                });
            }
        });
    }

    public static PythonPool newFixedPool(int nContexts) {
        return new PythonPool(nContexts);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        threadPool.execute(command);
    }

    @Override
    public void shutdown() {
        threadPool.shutdown();
        contexts.stream().forEach(c -> c.close());
        contexts.clear();
    }

    @Override
    public List<Runnable> shutdownNow() {
        var r = threadPool.shutdownNow();
        contexts.stream().forEach(c -> c.close(true));
        contexts.clear();
        return r;
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isTerminated();
    }
}
