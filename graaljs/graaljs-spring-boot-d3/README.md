## SVG Server-Side Rendering with Spring Boot and GraalJS

This Spring Boot application uses [GraalJS](https://www.graalvm.org/javascript/) to render [this chord diagram](https://observablehq.com/@d3/chord-diagram/2) with [D3.js](https://d3js.org/what-is-d3) on the server.

Because D3.js typically runs in a browser, use [linkedom](https://www.npmjs.com/package/linkedom/) to simulate a minimal Document Object Model (DOM) inside the JavaScript context.
This lets D3.js render SVG elements.

Write the JavaScript code that generates the SVG in modern ES6 syntax and bundle it with [Webpack](https://webpack.js.org/).

This bundle includes D3.js and the DOM simulation logic, and you can execute it on the Java backend using the [GraalVM Polyglot API](https://www.graalvm.org/latest/reference-manual/embed-languages/).

After rendering the SVG, the server extracts it as a string and passes it to the [Thymeleaf](https://www.thymeleaf.org/) view layer.
The client receives a fully rendered SVG, with no need for client-side rendering or JavaScript execution.

---

## Prerequisites

Before starting, make sure you have the following:

* A bit of time to explore and experiment
* Your favorite IDE or text editor for coding comfortably
* JDK 21 or later

---

## Run the Application

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/graalvm/graal-languages-demos.git
    cd graaljs/graaljs-spring-boot-d3
    ```

2.  **Build and run the Spring Boot application:**

    ```bash
    ./mvnw spring-boot:run
    ```

3. Open [http://localhost:8080/d3-chord](http://localhost:8080/d3-chord) and you'll see an SVG chord diagram rendered **server-side** with no client JS required.

---

## How It Works

### 1. Frontend (D3.js + linkedom)

`d3-chord.js`

1. creates a fake `document` using `linkedom`
2. generates a chord diagram using D3
3. extracts the generated SVG as a string
4. exports a function `renderChord` that takes `width` and `height` as arguments and returns the SVG string

```js
// In d3-chord.js
globalThis.document = parseHTML('<html><body></body></html>').document;
// ... D3.js chord diagram generation logic ...
function renderChord(width, height) {
    // ... D3 rendering logic ...
    return svg.node().outerHTML;
}
globalThis.renderChord = renderChord;
```

### 2. Webpack Bundling

Use webpack to bundle the code into a single file:

```js
// webpack.config.js
entry: './d3-chord.js',
output: {
  filename: 'd3-chord.bundle.js',
  libraryTarget: 'umd'
}
```

### 3. Java Backend with GraalJS

`D3Service.java`

1. uses GraalVM's polyglot API to load and evaluate the JavaScript bundle
2. retrieves the `renderChord` function from the JavaScript context
3. uses a pool of `renderChord` functions to maximize throughput
4. invokes one of the `renderChord` functions with specified dimensions per request
5. passes the returned SVG string to Thymeleaf for rendering in HTML

```java
// ...
public String renderChord(Model model) {
    // ...
    renderChordFunction = renderChordFunctionPool.take();
    model.addAttribute("svgContent", renderChordFunction.apply(640, 640));
    return "d3-chord";
    // ...
}
// ...
```
