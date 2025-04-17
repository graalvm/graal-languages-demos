function generateExcel(data) {
  var x=  new Promise((resolve, reject) => {
    var start = Date.now();
    const go = new Go();
    global.excelize = {};

    WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject)
      .then((result) => {
        var endInit = Date.now();
        go.run(result.instance);
        const f = excelize.NewFile();

        
        // Use the data passed as parameter
        data.forEach((row, idx) => {
          const ret1 = excelize.CoordinatesToCellName(1, idx + 1);
          if (ret1.error) {
            console.log(ret1.error);
            reject(ret1.error);
            return;
          }
          const res2 = f.SetSheetRow('Sheet1', ret1.cell, row);
          if (res2.error) {
            console.log(res2.error);
            reject(res2.error);
            return;
          }
        });
        
        const ret1 = f.GetCellValue('Sheet1', 'B1');
        if (ret1.error) {
          console.log(ret1.error);
          reject(ret1.error);
          return;
        }
        
        const { buffer, error } = f.WriteToBuffer();
        if (error) {
          console.error(error);
          reject(error);
        } else {
          // Expose the buffer to Java
          Polyglot.export("excelBuffer", buffer);
          resolve(buffer);
        }
      })
      .catch(error => {
        console.error("Failed to instantiate WebAssembly:", error);
        reject(error);
      });
  })

}

function readExcel() {
  let result ;

  let x =new Promise((resolve, reject) => {
    var start = Date.now();
    const go = new Go();
    global.excelize = {};

    WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject)
      .then((result) => {
        var endInit = Date.now();
        go.run(result.instance);

        const f = excelize.OpenReader(new Uint8Array(excelFileBytes)); // No fs.readFileSync

        // Get the value from cell B2
        const ret1 = f.GetCellValue('Sheet1', 'B2');
        if (ret1.error) {
          console.error(ret1.error);
          reject(ret1.error);  // Reject promise in case of error
        }

        // Get all rows from Sheet1
        const ret2 = f.GetRows('Sheet1');
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

  x.then(value=>{
  result = value;
  })
  console.log(result)
  return result;
}

