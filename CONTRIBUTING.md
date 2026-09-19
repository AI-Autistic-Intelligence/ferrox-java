# Contributing to Ferrox-Java

First off, thank you for considering contributing to Ferrox-Java!

## 1. Where do I go from here?

If you've noticed a bug or have a feature request, make sure to check if there's already an open issue. If not, feel free to open one.

## 2. Fork & pull is a great way to contribute
1. Fork the repo.
2. Create a new branch for your feature or bug fix.
3. Make sure the test suite passes (`./gradlew test`).
4. Submit a pull request.

## 3. Code Style
This project targets **Java 21**. 
- Please make use of modern Java features where applicable (Records, Pattern Matching, Virtual Threads, Foreign Memory API).
- Do not introduce blocking I/O calls where a non-blocking or singleflight approach is required.

## 4. Tests
We strive for **100% Test Coverage**. Any PR that lowers coverage will be rejected. 
- Please include JUnit 5 tests.
- For testing asynchronous logic on Virtual Threads, utilize `CountDownLatch` or `Awaitility`.
