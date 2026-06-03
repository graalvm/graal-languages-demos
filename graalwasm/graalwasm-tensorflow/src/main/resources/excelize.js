function readExcel(excelFileBytes) {
  let result ;
  return new Promise((resolve, reject) => {
    var start = Date.now();
    const go = new Go();
    global.excelize = {};

    WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject)
      .then((result) => {
        var endInit = Date.now();
        go.run(result.instance);

        const f = excelize.OpenReader(new Uint8Array(excelFileBytes)); // No fs.readFileSync


        const ret1 = f.GetCellValue('data', 'B2');
        if (ret1.error) {
          console.error(ret1.error);
          reject(ret1.error);  // Reject promise in case of error
        }

        // Get all rows from Sheet1
        const ret2 = f.GetRows('data');
        if (ret2.error) {
          console.error(ret2.error);
          reject(ret2.error);  // Reject promise in case of error
        } else {
          // Format the rows into a simple array format for return
          const resultArray = ret2.result.map(row => row.map(colCell => colCell));
          console.log("Extracted data:", resultArray);


          // Resolve the promise with the result array

          Polyglot.export("resultArray", resultArray);
          resolve(resultArray);
        }

        console.log("Excel read successfully.");
      })
      .catch(err => {
        console.error("Error reading Excel:", err);
        reject(err);  // Reject promise on error
      });
  });
}


