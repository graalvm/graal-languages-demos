async function main(wasmData) {
    try {
        const go = new Go();
        const {instance} = await WebAssembly.instantiate(
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