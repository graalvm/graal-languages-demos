import * as pdfjsLib from 'pdfjs-dist';
import * as worker from 'pdfjs-dist/build/pdf.worker.mjs';

globalThis.pdfjsWorker = {
  WorkerMessageHandler: worker.WorkerMessageHandler,
};

export async function pdfToHtml(javaBytes) {
  try {
    const data = globalThis.javaBytesToArrayBuffer(javaBytes);
    const loadingTask = pdfjsLib.getDocument({ data });
    const pdf = await loadingTask.promise;

    let html = '<meta charset="UTF-8">';

    for (let i = 1; i <= pdf.numPages; i++) {
      const page = await pdf.getPage(i);
      const content = await page.getTextContent({ disableCombineTextItems: false });

      html += `<div class="page" id="page_${i}">\n`;

      let lastY = null;

      for (const item of content.items) {
        const tx = item.transform;
        const y = tx[5];

        if (lastY !== null && Math.abs(y - lastY) > 5) {
          html += "<br>\n";
        }

        html += `<span>${item.str}</span>`;
        lastY = y;
      }

      html += "</div>\n";
    }

    return html;
  } catch (e) {
    throw e;
  }
}

globalThis.pdfToHtml = pdfToHtml;
