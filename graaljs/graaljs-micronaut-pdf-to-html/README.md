# PDF to HTML with Micronaut and GraalJS

---

This project demonstrates how to convert a PDF file into HTML using **[pdf.js](https://www.npmjs.com/package/pdfjs-dist)** inside a **[Micronaut](https://micronaut.io/)** application running on **[GraalVM JavaScript (GraalJS)](https://www.graalvm.org/latest/reference-manual/js/)**. The PDF data is loaded in Java, then passed to JavaScript where **pdf.js** performs the conversion. The resulting HTML is returned to Java and served for rendering in the browser.

---

## What you’ll need

Before you begin, you’ll need the following:

* Some time on your hands
* A decent text editor or IDE
* A supported JDK, preferably the latest [GraalVM JDK](https://www.graalvm.org/downloads/)

---

## Getting Started

1. **Clone the repository:**

   ```bash
   git clone https://github.com/graalvm/graal-languages-demos.git
   cd graaljs/graaljs-micronaut-pdf-to-html
   ```

2. **Add your PDF file:**

   Place your PDF (for example, `PDFHTML.pdf`) into the `src/main/resources/` directory.

3. **Build and run the app:**

   ```bash
   ./mvnw mn:run
   ```

---

## How It Works

* You write a JavaScript script (`pdf-to-html.js`) that uses **pdf.js** to read the PDF and convert it to HTML.
* Because GraalJS does not provide all browser APIs, a small, separate `polyfill.js` adds missing features needed by **pdf.js**.
* The JavaScript script is bundled with **[Webpack](https://webpack.js.org/)** into a single file (`pdf-to-html.bundle.js`).
* On the Java side, a Micronaut service reads the PDF file bytes, evaluates the polyfills and bundled script, then calls the exposed JavaScript function `pdfToHtml`, passing the PDF bytes.
* The JavaScript extracts the PDF content and converts it to HTML.
* A Micronaut controller exposes an HTTP endpoint (`/`) that calls the service and returns the HTML output, rendering the PDF content in the browser.

---

## Accessing the Output

Once the server is running, open:

[http://localhost:8080](http://localhost:8080)

You’ll see your PDF content rendered as HTML in the browser.

---
