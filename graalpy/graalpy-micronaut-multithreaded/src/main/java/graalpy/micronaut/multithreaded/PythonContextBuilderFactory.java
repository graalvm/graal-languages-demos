package graalpy.micronaut.multithreaded;

import java.io.IOException;
import java.nio.file.Path;

import org.graalvm.polyglot.Context;
import org.graalvm.python.embedding.utils.GraalPyResources;
import org.graalvm.python.embedding.utils.VirtualFileSystem;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.graal.graalpy.GraalPyContextBuilderFactory;
import jakarta.inject.Singleton;

@Bean
@Singleton
@Replaces(GraalPyContextBuilderFactory.class)
public class PythonContextBuilderFactory implements GraalPyContextBuilderFactory {
    @Override
    public Context.Builder createBuilder() {
        var resourcesDir = Path.of(System.getProperty("user.dir"), "graalpy.resources.single");
        var rf = resourcesDir.toFile();
        synchronized (PythonContextBuilderFactory.class) {
            if (!rf.isDirectory() || rf.lastModified() / 1000 < ProcessHandle.current().info().startInstant().get().getEpochSecond()) {
                var fs = VirtualFileSystem.create();
                try {
                    GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return GraalPyResources.contextBuilder(resourcesDir)
            .allowExperimentalOptions(true)
            .option("python.WarnExperimentalFeatures", "false")
            .option("python.IsolateNativeModules", "true")
            .allowNativeAccess(true)
            .allowCreateProcess(true);
    }
}
