## SVG Server-Side Rendering with Spring Boot and GraalJS

This project is a [Spring Boot](https://spring.io/projects/spring-boot) application that performs **server-side rendering of dynamic SVG graphs** using [D3.js](https://d3js.org/what-is-d3) executed within [GraalVM's JavaScript runtime (GraalJS)](https://www.graalvm.org/latest/reference-manual/js/). Since D3.js typically runs in a browser environment, we use [Linkedom](https://www.npmjs.com/package/linkedom/v/0.1.34) to simulate a minimal DOM (Document Object Model) inside the JavaScript context, allowing D3 to manipulate SVG elements as if it were in a real browser.

The JavaScript code responsible for generating the SVG is written in modern ES6 syntax and bundled using [Webpack](https://webpack.js.org/). This bundle includes D3.js and the DOM simulation logic, and is executed from the Java backend using [GraalVM Polyglot API](https://www.graalvm.org/latest/reference-manual/embed-languages/). Once the SVG is generated, it is extracted as a string and passed to the server-side view layer [Thymeleaf](https://www.thymeleaf.org/), enabling the client to receive a fully-rendered SVG, with no need for client-side rendering or JavaScript execution.

---

## Prerequisites

Before starting, make sure you have the following installed:

* A bit of time to explore and experiment
* Your favorite text editor or IDE to code comfortably
* JDK 21 or later
* Maven 3.9.9 or later

---

## How It Works

### 1. Frontend (D3 + Linkedom)

In `d3-chord.js`, we:

- Create a fake `document` using `linkedom`
- Generate a chord diagram using D3
- Extract the generated SVG as a string
- Export a function `renderChord` that takes `width` and `height` as arguments and returns the SVG string.

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

### 2\. Webpack Bundling

Webpack bundles the code into a single file:

```js
// webpack.config.js
entry: './d3-chord.js',
output: {
  filename: 'd3-chord.bundle.js',
  libraryTarget: 'umd'
}
```

### 3\. Java Backend with Graal.js

In `D3Service.java`, we:

- Use GraalVM's polyglot API to load and evaluate the JavaScript bundle.
- Retrieve the `renderChord` function from the JavaScript context.
- Execute `renderChord` with specified dimensions.
- Pass the returned SVG string to Thymeleaf for rendering in HTML.

<!-- end list -->

```java
// In D3Service.java
// ...
RenderChordFunction renderChordFunction = context.getBindings("js").getMember("renderChord").as(RenderChordFunction.class);
model.addAttribute("svgContent", renderChordFunction.apply(640, 640));
// ...
```

-----

## Installation Steps

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/graalvm/graal-languages-demos.git
    cd graaljs/graaljs-spring-boot-d3
    ```

2.  **Build and run the Spring Boot application:**
 
    ```bash
    ./mvnw spring-boot:run
    ```

-----

## Result

Open [http://localhost:8080/d3-chord](http://localhost:8080/d3-chord) and you'll see an SVG chord diagram rendered **server-side** with no client JS required.

-----

## Why Graal.js?

- Run JavaScript natively inside a Java app (no need for external Node.js)
- Secure, fast, and polyglot execution
- Perfect for SSR (Server-Side Rendering), data transformation, or embedded logic

