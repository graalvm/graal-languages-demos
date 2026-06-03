import 'fast-text-encoding';
import * as tf from '@tensorflow/tfjs';
import { setWasmPaths } from '@tensorflow/tfjs-backend-wasm';

// Set backend to WebAssembly
setWasmPaths('./');
await tf.setBackend('wasm');
await tf.ready();
globalThis.predictHouse = async function(houseFeatures) {
    const model = await tf.loadLayersModel({
        load: async () => savedArtifacts
    });
    const inputTensor = tf.tensor2d([houseFeatures]);

    const prediction = model.predict(inputTensor);
    prediction.print();
    return prediction.array();
};

let model;  //

globalThis.savedArtifacts = null;
globalThis.trainModel = function(datasetPromise) {
    datasetPromise.then(dataset => {
        const inputs = [];
        const prices = [];

        const dataRows = dataset.slice(1); // Skip header
        dataRows.forEach(row => {
            const numbers = row.map(val => {
                const num = Number(val);
                return isNaN(num) ? 0 : num;
            });

            const [price, ...features] = numbers;
            prices.push([price]);
            inputs.push(features);
        });

        const featureTensor = tf.tensor2d(inputs);
        const min = featureTensor.min(0);
        const max = featureTensor.max(0);
        const range = max.sub(min);
        const safeRange = range.add(tf.tensor1d(Array(range.shape[0]).fill(1e-7)));
        const normalizedFeatures = featureTensor.sub(min).div(safeRange);

        const labelTensor = tf.tensor2d(prices);

        model = tf.sequential();
        model.add(tf.layers.dense({ inputShape: [normalizedFeatures.shape[1]], units: 12, activation: 'relu' }));
        model.add(tf.layers.dense({ units: 6, activation: 'relu' }));
        model.add(tf.layers.dense({ units: 1 }));

        model.compile({ optimizer: 'adam', loss: 'meanSquaredError' });

        model.fit(normalizedFeatures, labelTensor, {
            epochs: 200,
            batchSize: 1,
            verbose: 1,
        }).then(() => {
            model.save({
                async save(modelArtifacts) {
                    savedArtifacts = modelArtifacts;
                    return {
                        modelArtifactsInfo: {
                            dateSaved: new Date(),
                            modelTopologyType: 'JSON',
                        }
                    };
                }
            }).then(() => {
                Polyglot.export("savedArtifacts", savedArtifacts);
            });
        });
    });
};

/*
async function save(modelArtifacts) {
    if (modelArtifacts.modelTopology instanceof ArrayBuffer) {
        throw new Error('BrowserLocalStorage.save() does not support saving model topology ' +
            'in binary formats yet.');
    }
    else {
        const topology = JSON.stringify(modelArtifacts.modelTopology);
        const weightSpecs = JSON.stringify(modelArtifacts.weightSpecs);
        const modelArtifactsInfo = getModelArtifactsInfoForJSON(modelArtifacts);
        // TODO(mattsoulanille): Support saving models over 2GB that exceed
        // Chrome's ArrayBuffer size limit.
        const weightBuffer = CompositeArrayBuffer.join(modelArtifacts.weightData);
        try {
            this.LS.setItem(this.keys.info, JSON.stringify(modelArtifactsInfo));
            this.LS.setItem(this.keys.topology, topology);
            this.LS.setItem(this.keys.weightSpecs, weightSpecs);
            this.LS.setItem(this.keys.weightData, arrayBufferToBase64String(weightBuffer));
            // Note that JSON.stringify doesn't write out keys that have undefined
            // values, so for some keys, we set undefined instead of a null-ish
            // value.
            const metadata = {
                format: modelArtifacts.format,
                generatedBy: modelArtifacts.generatedBy,
                convertedBy: modelArtifacts.convertedBy,
                signature: modelArtifacts.signature != null ?
                    modelArtifacts.signature :
                    undefined,
                userDefinedMetadata: modelArtifacts.userDefinedMetadata != null ?
                    modelArtifacts.userDefinedMetadata :
                    undefined,
                modelInitializer: modelArtifacts.modelInitializer != null ?
                    modelArtifacts.modelInitializer :
                    undefined,
                initializerSignature: modelArtifacts.initializerSignature != null ?
                    modelArtifacts.initializerSignature :
                    undefined,
                trainingConfig: modelArtifacts.trainingConfig != null ?
                    modelArtifacts.trainingConfig :
                    undefined
            };
            this.LS.setItem(this.keys.modelMetadata, JSON.stringify(metadata));
            return { modelArtifactsInfo };
        }
        catch (err) {
            // If saving failed, clean up all items saved so far.
            removeItems(this.keys);
            throw new Error(`Failed to save model '${this.modelPath}' to local storage: ` +
                `size quota being exceeded is a possible cause of this failure: ` +
                `modelTopologyBytes=${modelArtifactsInfo.modelTopologyBytes}, ` +
                `weightSpecsBytes=${modelArtifactsInfo.weightSpecsBytes}, ` +
                `weightDataBytes=${modelArtifactsInfo.weightDataBytes}.`);
        }
    }
}
*/