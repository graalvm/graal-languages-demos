package graalpy.micronaut.multithreaded;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;

@io.micronaut.context.annotation.Context
public class PythonPool extends AbstractExecutorService {
    private final Engine engine;
    private final ThreadLocal<Context> context;
    private final BlockingDeque<Context> contexts;
    private int size;

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
            .allowNativeAccess(true)
            .allowCreateProcess(true)
            .allowExperimentalOptions(true)
            .option("python.IsolateNativeModules", "true")
            .build();
        context.initialize("python");
        return context;
    }

    public Value eval(String code) {
        var c = context.get();
        if (c == null) {
            throw new IllegalStateException("PythonPool#eval can only be called from inside a submitted task");
        }
        return c.eval("python", code);
    }

    public synchronized void setPoolSize(int newSize) {
        if (size == newSize) {
            return;
        }
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

    public PythonPool() {
        engine = Engine.create();
        context = new ThreadLocal<>();
        contexts = new LinkedBlockingDeque<>();
        size = 0;
        setPoolSize(2);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public void execute(Runnable command) {
        Context c;
        try {
            c = contexts.takeFirst();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        contexts.addLast(c);
        context.set(c);
        try {
            command.run();
        } finally {
            context.remove();
        }
    }

    @Override
    public void shutdown() {
        doShutdown(false);
    }

    @Override
    public List<Runnable> shutdownNow() {
        doShutdown(true);
        return List.of();
    }

    private void doShutdown(boolean force) {
        contexts.stream().forEach(c -> c.close(force));
        contexts.clear();
    }

    @Override
    public boolean isShutdown() {
        return contexts.isEmpty();
    }

    @Override
    public boolean isTerminated() {
        return contexts.isEmpty();
    }
}
