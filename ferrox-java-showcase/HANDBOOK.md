# Core Banking Showcase - Programmer & User Handbook

## 1. Executive Summary

This handbook serves as the definitive architecture manual for the **Ferrox-Java Banking Showcase**, the official reference architecture for the Ferrox-Java ecosystem.

As a Senior Engineer, I recognize that the JVM is the backbone of the global financial system. However, traditional Java frameworks often suffer from "stop-the-world" Garbage Collection (GC) pauses and bloated, heavily-coupled monoliths. 

To solve this, I engineered this showcase around **Hexagonal Architecture (Ports and Adapters)**, **Project Reactor (WebFlux)**, and **Off-Heap Memory Management**. It provides deterministic sub-millisecond latency while maintaining absolute security through **PASETO v4** and immutable **Event Sourcing**.

---

## 2. Architectural Blueprint

### 2.1 The Domain Problem
A core banking ledger must process thousands of transactions per second. It must guarantee that an account balance cannot fall below zero due to race conditions. Furthermore, regulators require a 100% auditable trail of every state change, and high-frequency trading clients require zero GC-induced latency spikes.

### 2.2 System Components
1. **Hexagonal Architecture (Ports and Adapters)**
   - The `ferrox-java-core` domain logic sits at the absolute center. It has **zero dependencies** on Spring Boot, WebFlux, or Hibernate.
   - **Primary Adapters**: REST Controllers and Kafka Consumers that drive the domain.
   - **Secondary Adapters**: JPA Repositories and external API clients driven by the domain.
2. **Event Sourcing & Immutable Ledgers**
   - Instead of storing `balance: 1000`, the system stores the events: `AccountOpened(0) -> Deposited(1500) -> Withdrawn(500)`.
   - The current state is derived by replaying the events. This ensures cryptographic auditability.
3. **Off-Heap Caching (`ferrox-java-offheap`)**
   - Java's Heap memory is managed by the Garbage Collector. Storing millions of objects in the Heap causes massive GC pauses.
   - We utilize direct ByteBuffers (via `sun.misc.Unsafe` or `ByteBuffer.allocateDirect`) to store cache data entirely outside the JVM's purview, resulting in zero-GC overhead.
4. **Reactive I/O (Spring WebFlux)**
   - All I/O operations (HTTP requests, DB calls via R2DBC) are non-blocking `Mono<T>` and `Flux<T>`, allowing a small pool of threads to handle massive concurrency.
5. **Security**
   - **Authentication**: Stateless, tamper-proof `PasetoTokenService` using `Pasetos.parserBuilder()`.
   - **Password Hashing**: Argon2id to resist GPU cracking, moving away from outdated BCrypt.

---

## 3. Programmer Handbook (Developer Guide)

### 3.1 Environment Setup
```bash
# Requires Java 21+ and Gradle
./gradlew build

# Run the showcase
./gradlew :ferrox-java-showcase:bootRun
```

### 3.2 Modifying the Hexagon
1. **Never import Spring or DB annotations into the Domain package.**
2. If the domain needs to save data, create an interface `LedgerPort` in the domain.
3. Implement `LedgerAdapter implements LedgerPort` in the infrastructure package and inject it.

### 3.3 Event Sourcing Best Practices
- **Events must be immutable.** Once an event is created and saved, it cannot be altered.
- **Versioning**: If the schema of an event changes, create a new class (e.g., `DepositedEventV2`) and configure the Upcaster to map V1 to V2 during replay.

### 3.4 Off-Heap Buffer Safety
When using `ferrox-java-offheap`:
- You are responsible for freeing memory. If you allocate an off-heap buffer and lose the reference without calling `buffer.release()`, you will cause a memory leak that the JVM cannot fix.
- Use try-with-resources where possible if the buffer implements `AutoCloseable`.

---

## 4. User & DevOps Handbook (Operations)

### 4.1 JVM Tuning & Deployment
Deploying this application requires specific JVM flags to optimize the Off-Heap allocations and WebFlux thread pools.
```bash
java -XX:MaxDirectMemorySize=4G \
     -Xms1G -Xmx1G \
     -XX:+UseZGC \
     -jar ferrox-java-showcase.jar
```
*Note: We cap the Heap (`-Xmx1G`) and allocate large Direct Memory (`MaxDirectMemorySize=4G`) because our custom caches operate off-heap.*

### 4.2 Metrics & Tracing
The application integrates Micrometer and OpenTelemetry.
- Ensure your cluster has an OpenTelemetry Collector available.
- Distributed traces will tag every transaction ID, allowing you to trace a deposit from the HTTP ingress, through the Hexagon, into the Event Store.

---

## 5. Senior Engineering Decisions

1. **Why Hexagonal over Layered Architecture?** In a standard Layered architecture, the Domain implicitly depends on the Database layer. If we migrate from PostgreSQL to Cassandra, the Domain breaks. Hexagonal inverts this: the Database layer depends on the Domain's Interfaces. This future-proofs the banking logic.
2. **Why Event Sourcing?** If a regulator audits a traditional SQL database, they only see the *current* state. They cannot prove *how* the state was reached. Event Sourcing provides mathematical proof of the ledger's history.
3. **Why PASETO over JWT?** JWTs allow developers to accidentally trust tokens with `alg: none`. PASETO completely removes algorithm negotiation from the header. The application strictly expects `v4.local`, guaranteeing AES-256-GCM symmetric encryption or Ed25519 signatures.
