# 📊 Micronaut Excel Exporter using GraalVM, Excelize.wasm & H2 Database

This Micronaut project demonstrates how to use **GraalVM**'s Polyglot capabilities (JavaScript + WebAssembly) to interact with the `excelize.wasm` module. It reads and writes Excel `.xlsx` files directly from your backend — no need for native Excel libraries in Java!

## 🔧 What This Project Does

- Accepts `.xlsx` files via a frontend and reads them into an H2 in-memory database.
- Automatically populates sample data when downloading an Excel file.
- Generates an Excel file from database contents using the `excelize.wasm` module executed via **GraalJS + GraalWasm**.
- Returns the Excel file for download in the browser.

## 🧠 What is Excelize?

**[Excelize](https://github.com/xuri/excelize)** is a popular Go library for creating and reading Microsoft Excel (xlsx) spreadsheets. In this project, we're using the **WebAssembly (WASM)** version of Excelize so that we can run it inside a Java application using GraalVM.

## 🚀 What is GraalVM, GraalJS, and GraalWasm?

- **GraalVM** is a high-performance runtime that supports multiple languages including JavaScript, Python, Ruby, and WebAssembly.
- **GraalJS** allows executing JavaScript from Java.
- **GraalWasm** allows executing **WASM binaries (like excelize.wasm)** from Java or JavaScript via GraalVM.
- Here, GraalVM runs a JavaScript wrapper that loads and uses `excelize.wasm` to interact with Excel files.


## 🖥️ How to Run It Locally

### ✅ Prerequisites

- [Java 17+](https://adoptium.net/)
- [GraalVM CE 22+](https://www.graalvm.org/)
- Enable **JavaScript and WASM** support in GraalVM
- Micronaut CLI or build tools (Gradle or Maven)

### 📥 Clone and Run

```bash
git clone https://github.com/anwarmoussaoui/excel-graalvm-demo.git
cd excel-graalvm-demo
./mvnw run
