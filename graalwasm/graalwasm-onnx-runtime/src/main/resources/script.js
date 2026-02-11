let session = null ;
async function predict(modelBuffer,dataX,dataY) {
  try {

                      const session = await ort.InferenceSession.create(new Uint8Array(modelBuffer));

                      // prepare inputs. a tensor need its corresponding TypedArray as data
                      const dataA = Float32Array.from(dataX);
                      const dataB = Float32Array.from(dataY);
                      const tensorA = new ort.Tensor('float32', dataA, [3, 4]);
                      const tensorB = new ort.Tensor('float32', dataB, [4, 3]);

                      // prepare feeds. use model input names as keys.
                      const feeds = { a: tensorA, b: tensorB };

                      // feed inputs and run
                      const results = await session.run(feeds);

                      // read from results
                      const dataC = results.c.data;
                      console.log(dataC);

                      return dataC;
                  } catch (e) {
                      document.write(`failed to inference ONNX model: ${e}.`);
                  }

}