# React.js SSR with Micronaut

This project demonstrates how to implement **Server-Side Rendering (SSR)** of **[React.js](https://reactjs.org/)** components within a **[Micronaut](https://micronaut.io/)** application using **[GraalJS](https://www.graalvm.org/latest/reference-manual/js/)**. It uses the **[Recharts](https://recharts.org/en-US/)** library to generate interactive charts that are rendered on the server and returned as HTML.

## Prerequisites

Before you begin, make sure you have the following installed:

* A bit of time to explore and experiment 🙂
* Your favorite text editor or IDE
* JDK 21 or later
* Maven 3.9.9 or later

## Installation Steps

1. **Clone the repository:**

   ```bash
   git clone https://github.com/graalvm/graal-languages-demos.git
   cd graaljs/graaljs-micronaut-react-ssr
   ```

2. **Build and run the Micronaut application:**

   ```bash
   ./mvnw mn:run 
   ```

---

## Result

Open your browser and go to [http://localhost:8080](http://localhost:8080) to view the server-rendered React charts. The charts are generated on the server and returned as HTML, providing fast initial page load and improved performance.

---

