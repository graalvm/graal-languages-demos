let f;
global.excelize = {};
const go = new Go();
WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject).then((result) => {go.run(result.instance);});

function generateExcel(data) {
  new Promise((resolve, reject) => {
           f = excelize.NewFile();
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

}

function readExcel(excelFileBytes) {
  let result ;

  return new Promise((resolve, reject) => {


         f = excelize.OpenReader(new Uint8Array(excelFileBytes)); // No fs.readFileSync



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

  });
}

