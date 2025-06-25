let session = null ;
async function predict(modelBuffer) {
  try {
    if(session ===null ) session = await ort.InferenceSession.create(new Uint8Array(modelBuffer));

    const input = new Float32Array([
      3, 1.5, 1340, Math.random()*2000, 1.5, 0, 0, 3, 1340, 0, 1955, 2005
    ]);
    const inputTensor = new ort.Tensor("float32", input, [1, input.length]);

    const feeds = { input: inputTensor };
    const results = await session.run(feeds);
    return results.output.data;
  } catch (e) {
    console.error("Failed to run model:", e);
  }
}