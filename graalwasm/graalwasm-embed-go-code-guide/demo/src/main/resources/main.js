async function main(wasmData) {
    try {
        // Polyfill for instantiateStreaming if needed
        if (!WebAssembly.instantiateStreaming) {
            WebAssembly.instantiateStreaming = async (sourcePromise, importObject) => {
                const source = await sourcePromise;
                return await WebAssembly.instantiate(source, importObject);
            };
        }

        const go = new Go();
        const { instance } = await WebAssembly.instantiate(
            new Uint8Array(wasmData),
            go.importObject
        );
        go.run(instance);
        console.log("Sum:", global.add(1, 2));
    } catch (err) {
        console.error("Error running WebAssembly:", err);
    }
}
main(wasmData);