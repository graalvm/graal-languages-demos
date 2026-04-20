package com.example.Tensorflow;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ContextPool {
    private final BlockingQueue<Context> contexts;

    private static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.performance", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.unhandled-rejections", "throw");
        options.put("js.commonjs-require-cwd", Paths.get("./").toAbsolutePath().toString());
        return options;
    }

        public ContextPool () throws IOException {

        Context context = Context.newBuilder("js", "wasm")
                .allowAllAccess(true)
                .options(getLanguageOptions())
                .build();


            ExcelizePool excelizePool = new ExcelizePool();
            Context context1 = excelizePool.getContext();
            byte[] excelBytes = Files.readAllBytes(Paths.get("./src/main/resources/data.xlsx"));


            Value readFunc = context1.getBindings("js").getMember("readExcel");
            Value bufferArray = readFunc.execute(excelBytes);


            byte[] tsfjswasm = Files.readAllBytes(Paths.get("./src/main/resources/tfjs-backend-wasm-simd.wasm"));
            context.getBindings("js").putMember("tsfwasm", tsfjswasm);

            String polyfill= """
                               (() => {
                              const NativeURL = globalThis.URL;
                    
                              class FakeURL {
                                constructor(input, base) {
                                  this.href = input;
                                }
                    
                                toString() {
                                  return this.href;
                                }
                              }
                    
                              globalThis.URL = FakeURL;
                    
                              globalThis.fetch = async function (url) {
                                const tsfwasm = './tfjs-backend-wasm-simd.wasm'
                                const target = (typeof url === 'object' && 'href' in url) ? url.href : url;
                                if (target === tsfwasm) {
                                  return {
                                    async arrayBuffer() {
                                      return globalThis.tsfwasm;
                                    },
                                    ok: true,
                                    status: 200,
                                  };
                                }
                                else {
                                  throw new Error(`Unhandled fetch to: ${target}`);
                                }
                              };
                            })();
                    if (typeof WebAssembly.instantiateStreaming !== "function") {
                      WebAssembly.instantiateStreaming = async (sourcePromise, importObject) => {
                        // Assume `globalThis.tsfwasm` is already a Uint8Array or ArrayBuffer
                        const buffer = globalThis.tsfwasm instanceof Uint8Array
                          ? globalThis.tsfwasm.buffer
                          : globalThis.tsfwasm;
                    
                        return WebAssembly.instantiate(new Uint8Array(buffer), importObject);
                      };
                    }
                    globalThis.self = globalThis;
                    globalThis.window = globalThis;
                    globalThis.document = { body: {} }
                    globalThis.window.location = { href: '' }
                    """
                    ;
            context.eval("js",polyfill);
            Source bundleSrc = Source.newBuilder("js",ContextPool.class.getResource("/bundle/bundle.mjs")).build();
            context.eval(bundleSrc);
            context.getBindings("js").getMember("trainModel").execute(bufferArray);
            System.out.println( context.getBindings("js").getMember("savedArtifacts"));


            int maxThreads = Runtime.getRuntime().availableProcessors();
            contexts = new LinkedBlockingQueue<>(maxThreads);
            for (int i = 0; i < maxThreads; i++) {
            Context modelContext = Context.newBuilder("js", "wasm")
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .options(getLanguageOptions())
                    .build();

            modelContext.getBindings("js").putMember("tsfwasm", tsfjswasm);
            modelContext.eval("js",polyfill);

            modelContext.eval(bundleSrc);
            Value mdl = context.getBindings("js").getMember("savedArtifacts");
            modelContext.getBindings("js").putMember("savedArtifacts",mdl);
            this.contexts.add(modelContext);

            }
    }

    public Context getContext() {
        try {
            return contexts.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    void release(Context context) {
        contexts.add(context);
    }
}
