# GraalPy Demos and Guides

This directory contains demo applications and guides for [GraalPy](https://www.graalvm.org/python/).

*Tip: open this directory in IntellIJ IDEA. It should offer to load all Maven projects.*

## Demos

- [Minimal Java application that embeds GraalPy](graalpy-starter/)
- [Minimal Java application that embeds `openai` Python package with GraalPy](graalpy-openai-starter/)
- [Embed `qrcode` Python package with GraalPy in JBang](graalpy-jbang-qrcode/)
- [Embed SVG charting library `pygal` with GraalPy in Micronaut](graalpy-micronaut-pygal-charts/)
- [Embed SVG charting library `pygal` with GraalPy in Spring Boot](graalpy-spring-boot-pygal-charts/)

## Guides

- Use GraalPy and GraalPy Maven plugin in a [Java SE application](graalpy-javase-guide/)
- Use GraalPy with popular Java frameworks, such as [Spring Boot](graalpy-spring-boot-guide/) or [Micronaut](graalpy-micronaut-guide/)
- Use GraalPy Maven plugin to install and use Python packages that rely on [native code](graalpy-native-extensions-guide/), e.g. for data science and machine learning
- Manually [install Python packages and files](graalpy-custom-venv-guide/) if the Maven plugin gives not enough control
- [Freeze](graalpy-freeze-dependencies-guide/) transitive Python dependencies for reproducible builds
- [Migrate from Jython](graalpy-jython-guide/) to GraalPy
