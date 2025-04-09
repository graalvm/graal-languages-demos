# GraalJS-Spring-Boot-D3 - Generating SVG Graphs with Spring Boot and GraalJS

## Description

This project is a Spring Boot application that generates and renders an SVG graph on the server side using D3.js and GraalVM's JavaScript runtime (GraalJS). It leverages Linkedom to simulate a browser-like DOM environment within a Node.js-compatible context, enabling server-side rendering of the graph before sending it to the client.

## Prerequisites

* Java 21+
* Maven
* GraalVM with JavaScript support (GraalJS)

## Technologies Used

* **Spring Boot:** Java backend framework
* **Thymeleaf:** Server-side templating engine
* **D3.js:** Data visualization library
* **GraalVM (GraalJS):** Polyglot runtime for executing JavaScript in Java
* **Linkedom:** DOM simulation for server-side rendering of D3.js

## How It Works

1.  **Backend Processing:**
    * A user accesses `/graph`.
    * The `GraphController` invokes the `GraphService` to generate the SVG graph.
    * The `GraphService`:
        * Reads the D3.js script (`graph.js`).
        * Uses GraalVM (GraalJS) to execute the script within a JavaScript context.
        * Simulates a DOM environment with Linkedom to enable D3.js to render the SVG.
        * Returns the generated SVG as a string.
    * The `GraphController` passes the SVG string to the Thymeleaf template (`graph.html`).
    * The page displays the pre-rendered graph.

2.  **Server-Side Rendering (SSR):**
    * The D3.js script runs on the server, not in the browser.
    * Linkedom provides a virtual DOM for D3.js to manipulate.
    * The final SVG is sent as raw HTML to the client.

## Installation Steps

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/graalvm/graal-languages-demos.git
    cd graaljs/graaljs-spring-boot-d3
    ```

2.  **Build and run the Spring Boot application:**

    ```bash
    mvn clean package
    mvn spring-boot:run
    ```

3.  **Open your browser and visit:**

    ```
    http://localhost:8082/graph
    ```


## Maven Project Setup

### 1. **Creating a New Project:**

**If you're looking to start from scratch and create a new one, follow these steps✨**

#### 1.1. **Setup Spring Boot Application**

* **Create a new Spring Boot project**:
    - You can generate a Spring Boot project from [Spring Initializr](https://start.spring.io/).
    - Add the dependencies: **Spring Web**, **Thymeleaf**.
* **Import the project into your IDE** (e.g., IntelliJ, Eclipse).

#### 1.2. **Add GraalJS and Polyglot Dependencies**

In your `pom.xml`, add the following dependencies for GraalJS and Polyglot API:

```xml
<dependencies>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>js</artifactId>
        <version>24.2.0</version>
        <type>pom</type>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```
Add the necessary plugins to handle both Spring Boot and frontend tasks in the <build> section of your pom.xml:

```xml
<build>
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        <!-- Frontend Maven Plugin -->
        <plugin>
            <groupId>com.github.eirslett</groupId>
            <artifactId>frontend-maven-plugin</artifactId>
            <version>1.12.1</version>
            <configuration>
                <nodeVersion>v22.14.0</nodeVersion>
                <workingDirectory>src/main/resources/static</workingDirectory>
                <installDirectory>target</installDirectory>
            </configuration>
            <executions>
                <execution>
                    <id>install node and npm</id>
                    <goals>
                        <goal>install-node-and-npm</goal>
                    </goals>
                    <phase>generate-resources</phase>
                </execution>
                <execution>
                    <id>npm install</id>
                    <goals>
                        <goal>npm</goal>
                    </goals>
                    <phase>generate-resources</phase>
                    <configuration>
                        <arguments>install</arguments>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 1.3. **Create GraphService**

Next, create the `GraphService` class to handle the logic for generating the graph.

```java
package com.example.d3demo;

import org.graalvm.polyglot.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GraphService {

    private final Engine sharedEngine = Engine.newBuilder().option("engine.TraceCompilation", "true").build();

    public String generateGraph() throws IOException {
        try (Context context = Context.newBuilder("js")
                .engine(sharedEngine)
                .allowIO(IOAccess.ALL)
                .allowExperimentalOptions(true)
                .option("js.commonjs-require", "true")
                .build()) {

            Source source = Source.newBuilder("js", new ClassPathResource("static/graph.js").getURL()).mimeType("application/javascript+module").build();
            Value result = context.eval(source);
            return result.asString();
        } catch (PolyglotException e) {
            throw new IOException("Error executing JavaScript code: " + e.getMessage(), e);
        }
    }
}
```

#### 1.4. **Create GraphController**

Add the following controller to handle requests and render the graph in the view:

```java
package com.example.d3demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class GraphController {

    private final GraphService graphService;

    GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph")
    public String displayGraph(Model model) {
        try {
            String svg = graphService.generateGraph();
            model.addAttribute("svgContent", svg);
            return "graph";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error generating the graph.");
            model.addAttribute("errorDetails", e.getMessage());
            return "error";
        }
    }
}
```

#### 1.5. **Create Thymeleaf Template: `graph.html`**

Create a file called `graph.html` in `src/main/resources/templates/`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>SVG Graph</title>
    <style>
        body {
            font-family: sans-serif;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            background-color: #f4f4f4;
            padding: 20px;
            box-sizing: border-box;
        }

        h1 {
            color: #333;
            margin-bottom: 20px;
            text-align: center;
            font-size: 2em;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
        }

        svg {
            max-width: 800px;
            width: 100%;
            height: auto;
            overflow: visible;
            display: block;
            margin: 20px auto;
        }

        svg text {
            font-size: 0.8em;
            fill: #555;
        }
    </style>
</head>
<body>
<h1>GraalVM Language Interactions</h1>
<svg th:utext="${svgContent}"></svg>
</body>
</html>
```

#### 1.6. **Create Error Template: `error.html`**

Create a simple error page `error.html` to handle errors if the graph generation fails:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Erreur</title>
</head>
<body>
<h1>Erreur</h1>
<p th:text="${errorMessage}"></p>
<p th:text="${errorDetails}"></p>
</body>
</html>
```

#### 1.7. **Add JavaScript Code (`graph.js`)**

Place the JavaScript code for generating the graph in `src/main/resources/static/graph.js`:

```javascript
var d3 = require('./node_modules/d3/dist/d3.js');
var linkedom = require('linkedom');

globalThis.document = linkedom.parseHTML('<html><body></body></html>').document;

const width = 600;
const height = 600;
const margin = 50;
const innerRadius = Math.min(width, height) * 0.5 - 60;
const outerRadius = innerRadius + 20;

const data = [
    [0, 20, 10, 5, 8, 3, 12, 1],
    [20, 0, 15, 7, 6, 9, 4, 2],
    [10, 15, 0, 12, 3, 5, 6, 8],
    [5, 7, 12, 0, 10, 1, 7, 3],
    [8, 6, 3, 10, 0, 14, 9, 11],
    [3, 9, 5, 1, 14, 0, 8, 6],
    [12, 4, 6, 7, 9, 8, 0, 10],
    [1, 2, 8, 3, 11, 6, 10, 0]
];

const names = ["Java", "JavaScript", "Python", "Ruby", "R", "C/C++", "Native Image", "Polyglot"];
const colors = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f"];

const chord = d3.chord()
    .padAngle(0.05)
    .sortSubgroups(d3.descending)
    .sortChords(d3.descending);

const chords = chord(data);

const arc = d3.arc()
    .innerRadius(innerRadius)
    .outerRadius(outerRadius);

const ribbon = d3.ribbon()
    .radius(innerRadius);

const color = d3.scaleOrdinal()
    .domain(names)
    .range(colors);

const svg = d3.create('svg')
    .attr('width', width)
    .attr('height', height)
    .append('g')
    .attr('transform', `translate(${width / 2},${height / 2})`);

svg.append('g')
    .selectAll('g')
    .data(chords.groups)
    .enter()
    .append('g')
    .append('path')
    .style('fill', d => color(names[d.index]))
    .style('stroke', 'black')
    .attr('d', arc);

svg.append('g')
    .selectAll('path')
    .data(chords)
    .enter()
    .append('path')
    .attr('d', ribbon)
    .style('fill', d => color(names[d.source.index]))
    .style('opacity', 0.8)
    .style('stroke', 'black');

svg.append('g')
    .selectAll('text')
    .data(chords.groups)
    .enter()
    .append('text')
    .each(d => { d.angle = (d.startAngle + d.endAngle) / 2; })
    .attr('dy', '.35em')
    .attr('transform', d => `
        rotate(${(d.angle * 180 / Math.PI - 90)})
        translate(${outerRadius + 10})
        ${d.angle > Math.PI ? 'rotate(180)' : ''}
    `)
    .style('text-anchor', d => d.angle > Math.PI ? 'end' : null)
    .text(d => names[d.index]);

module.exports = svg.node().outerHTML;
```

### 2. **Building and Testing the Application**

Now, compile and run the application:

```bash
./mvnw package
./mvnw spring-boot:run
```

Visit the URL `http://localhost:8080/graph` in your browser to see the generated graph.

## Conclusion

By following this guide, you have learned how to:

-   Execute D3.js JavaScript code within a Java application using GraalJS.
-   Use `linkedom` to simulate the DOM environment on the server side.

