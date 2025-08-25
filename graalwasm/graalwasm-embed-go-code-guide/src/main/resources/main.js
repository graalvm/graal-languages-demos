async function main(wasmBytes) {
    try {
        const go = new Go();
        const {instance} = await WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject);
        go.run(instance);

        // The "add" function is now available on the global object
        const result = globalThis.add(3, 4);
        console.log("3 + 4 =", result);
    } catch (err) {
        console.error("Error running WebAssembly:", err);
    }
}

main(wasmBytes);
