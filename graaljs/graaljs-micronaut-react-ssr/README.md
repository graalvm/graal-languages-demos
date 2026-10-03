# React SSR with Micronaut

This project demonstrates how to render [various Recharts examples](https://recharts.org/en-US/examples) in a [Micronaut](https://micronaut.io/) application.
For this, it uses the [React view feature](https://micronaut-projects.github.io/micronaut-views/latest/guide/#react) in Micronaut that enables Server-Side Rendering (SSR) of React components using [GraalJS](https://www.graalvm.org/javascript/).

## Prerequisites

Before you begin, make sure you have the following:

* A bit of time to explore and experiment
* Your favorite text editor or IDE
* JDK 21 or later

## Run the Application

1. Clone the repository:

   ```bash
   git clone https://github.com/graalvm/graal-languages-demos.git
   cd graaljs/graaljs-micronaut-react-ssr
   ```

2. Build and run the Micronaut application:

   ```bash
   ./mvnw mn:run 
   ```

3. Open your browser and go to [http://localhost:8080](http://localhost:8080) to view the Recharts examples.
   Since the charts are rendered on the server, they work even with JavaScript disabled in the browser.

