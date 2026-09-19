<p align="center">
  <h1 align="center">Ferrox-Java</h1>
  <p align="center">
    <strong>Enterprise-grade Java 21 adaptation of the Rust `ferrox` ecosystem.</strong>
    <br/>
    Extreme concurrency, zero-trust security, off-heap memory, and metaprogramming paradigms traditionally reserved for low-level systems.
  </p>
</p>

## 🚀 Features

- **Project Loom (Virtual Threads) Native:** True synchronous, non-blocking I/O. Handles millions of concurrent connections without WebFlux callback hell.
- **Cache Stampede Prevention (Singleflight):** Eliminates database dogpiling. 10,000 concurrent threads asking for the same key result in exactly **1** database query.
- **Zero-Trust Security (Sentinel Threat Engine):** Real-time Shannon Entropy analysis drops heavily obfuscated payloads before they reach business logic. Cryptographic parity with Rust via PASETO v4 & Argon2id.
- **Off-Heap Zero-GC Memory (Project Panama):** Stores ultra-hot data directly in OS RAM via `MemorySegment`. Zero Garbage Collection pauses. Perfect for High-Frequency Trading.
- **Compile-Time Metaprogramming (APT):** Zero-overhead Reflection-less schema generation at compile time (mirroring Rust's `#[derive(FerroxEntity)]`).
- **Asynchronous EventBus:** Highly scalable in-process Pub/Sub utilizing Virtual Threads.

## 📦 Modules

| Module | Description |
|---|---|
| `ferrox-java-core` | Fundamental abstractions & Error Taxonomy. |
| `ferrox-java-security` | Sentinel Threat Engine & PASETO Auth. |
| `ferrox-java-data` | Singleflight Concurrency Control. |
| `ferrox-java-cqrs` | Strict Command & Query Bus segregation. |
| `ferrox-java-crud-gen` | APT Code Generators. |
| `ferrox-java-event-manager` | In-memory Pub/Sub Event Sourcing. |
| `ferrox-java-observability` | Pino-style JSON logging & Correlation IDs. |
| `ferrox-java-resilience` | Virtual-thread optimized Circuit Breaker. |
| `ferrox-java-offheap` | Zero-GC Arena Allocators. |

## 🛠️ Usage

To import Ferrox-Java into your project via GitHub Packages, add the following to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/YOUR_ORG/ferrox-java")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("dev.ferrox:ferrox-java-core:1.0.0")
    // Add other modules as needed...
}
```

## 📖 Documentation
Comprehensive architectural documentation is available in the `ferrox-docs` Docusaurus repository.

## 📄 License
This project is licensed under the MIT License.
