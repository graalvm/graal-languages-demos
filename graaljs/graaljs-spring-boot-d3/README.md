## SVG Server-Side Rendering with Spring Boot and GraalJS

This project is a Spring Boot application that uses [GraalJS](https://www.graalvm.org/javascript/) to render [this chord diagram](https://observablehq.com/@d3/chord-diagram/2) using [D3.js](https://d3js.org/what-is-d3) on the server.
Since D3.js typically runs in a browser environment, this project uses [linkedom](https://www.npmjs.com/package/linkedom/) to simulate a minimal Document Object Model (DOM) inside the JavaScript context, allowing D3.js to render SVG elements.

The JavaScript code responsible for generating the SVG is written in modern ES6 syntax and bundled using [Webpack](https://webpack.js.org/).
This bundle includes D3.js and the DOM simulation logic, and is executed on the Java backend using [GraalVM Polyglot API](https://www.graalvm.org/latest/reference-manual/embed-languages/).
Once the SVG is rendered, it is extracted as a string and passed to the server-side view layer [Thymeleaf](https://www.thymeleaf.org/), enabling the client to receive a fully-rendered SVG, with no need for client-side rendering or JavaScript execution.

---

## Prerequisites

Before starting, make sure you have the following:

* A bit of time to explore and experiment
* Your favorite IDE or text editor to code comfortably
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

In `d3-chord.js`, we:

- create a fake `document` using `linkedom`
- generate a chord diagram using D3
- extract the generated SVG as a string
- export a function `renderChord` that takes `width` and `height` as arguments and returns the SVG string.

```js
// In d3-chord.js
globalThis.document = parseHTML('<html><body></body></html>').document;
// ... D3.js chord diagram generation logic ...
function renderChord(width, height) {
    // ... D3 rendering logic ...
    return svg.node().outerHTML;
}
globalThis.renderChord = renderChord;
````

### 2. Webpack Bundling

Webpack bundles the code into a single file:

```js
// webpack.config.js
entry: './d3-chord.js',
output: {
  filename: 'd3-chord.bundle.js',
  libraryTarget: 'umd'
}
```

### 3. Java Backend with GraalJS

In `D3Service.java`, we:

- use GraalVM's polyglot API to load and evaluate the JavaScript bundle.
- retrieve the `renderChord` function from the JavaScript context.
- use a pool of `renderChord` functions to maximize throughput.
- invoke one of the `renderChord` functions with specified dimensions per request.
- pass the returned SVG string to Thymeleaf for rendering in HTML.

```java
// In D3Service.java
// ...
RenderChordFunction renderChordFunction = context.getBindings("js").getMember("renderChord").as(RenderChordFunction.class);
model.addAttribute("svgContent", renderChordFunction.apply(640, 640));
// ...
```
