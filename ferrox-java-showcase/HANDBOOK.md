# Ferrox-Java Core Banking - The Definitive Handbook

## 1. Executive Summary & Senior Engineering Vision

This document is the absolute engineering manual for the **Ferrox-Java Banking Showcase**, the official reference architecture for the Ferrox ecosystem on the JVM.

The JVM powers the world’s most critical financial networks. Yet, massive Spring Boot monoliths hide the hardware from the developer, leading to catastrophic Garbage Collection (GC) pauses during peak trading hours. As a Senior Engineer, I architected this banking ledger using **Hexagonal Architecture**, **Event Sourcing**, and **Off-Heap Direct Memory Management**. 

This system guarantees deterministic, sub-millisecond latency by bypassing the JVM heap while leveraging zero-copy OS kernel features, providing an unshakeable foundation for high-frequency finance.

---

## 2. The Domain Problem

A core banking ledger is fundamentally different from a CRUD app. 
1. **Auditability**: Regulators demand cryptographic proof of state changes. Overwriting balances via `UPDATE` statements destroys history.
2. **Concurrency**: Thousands of transactions per second hitting the same account must not cause race conditions.
3. **Latency**: A 50ms GC pause during a market flash-crash can ruin quantitative strategies.

---

## 3. Low-Level Architecture & OS-Kernel Interactions

### 3.1 Off-Heap Memory Paradigm (`ferrox-java-offheap`)
To eliminate GC "Stop-The-World" pauses, we prevent objects from ever reaching the heap.
- **Bypassing the Heap**: Using `ByteBuffer.allocateDirect()` and `sun.misc.Unsafe`, we map contiguous blocks of memory directly in the OS virtual memory space using the `mmap()` syscall. This memory is invisible to the GC's root traversal.
- **Mechanical Sympathy & NUMA**: We implement lock-free queues (Disruptor patterns) in off-heap memory. Instead of OS-level mutexes (which force thread context switches), we use CPU memory barriers (`Unsafe.loadFence()`, `storeFence()`). This synchronizes L1/L2 caches across multiple CPU sockets (NUMA nodes) entirely at the hardware level.

### 3.2 Network I/O and Zero-Copy (Netty)
- **Sendfile Syscall**: For large payloads, Netty uses zero-copy. Instead of copying data `Disk -> Kernel -> JVM Heap -> Kernel -> NIC`, we invoke `sendfile()`. The Linux kernel transfers data directly from the disk buffer to the Network Interface Card (NIC), saving massive RAM bandwidth.
- **Edge-Triggered Epoll**: Netty configures Linux `epoll` in edge-triggered mode (`EPOLLET`), drastically cutting down syscall overhead for active TCP connections.

---

## 4. Application Architecture & Distributed Patterns

### 4.1 Hexagonal Architecture (Ports and Adapters)
- **The Core**: The `Domain` package has **zero dependencies** on Spring, Jackson, or JDBC. It contains pure Java financial logic.
- **Inversion of Control**: The domain exposes interfaces (`Ports`). The infrastructure (`Adapters`) implements them. The database depends on the domain, ensuring we can swap PostgreSQL for Cassandra without touching the core banking logic.

### 4.2 Event Sourcing & Immutable Ledgers
- **State Derived from History**: We do not store `Balance = $1000`. We store an immutable stream: `AccountOpened -> Deposited($1500) -> Withdrawn($500)`. Replaying events derives the state.
- **Append-Only Performance**: By only running `INSERT` queries on the DB, we eliminate row-level locks and `UPDATE` contention. Sequential writes to NVMe drives perfectly align with file system block boundaries for maximum disk throughput.

---

## 5. Security Model: Zero-Trust & Cryptography

### 5.1 PASETO v4 Local vs JWT
JWT is fundamentally flawed because the token specifies its own encryption algorithm in the header, enabling downgrade attacks (`alg: none`).
- **Cryptographic Rigidity**: PASETO v4 Local enforces `XChaCha20-Poly1305` authenticated encryption. The algorithm is fixed into the protocol.
- **Constant-Time Verification**: PASETO signatures are verified using constant-time CPU instructions. This prevents attackers from measuring nanosecond latency differences over the network to forge keys via Timing Attacks.

### 5.2 Password Hashing
We utilize **Argon2id** configured for strict memory-hardness, rendering GPU parallel cracking arrays useless, deprecating BCrypt.

---

## 6. Programmer's Guide (Developer Workflow)

### 6.1 Environment Setup
```bash
# Requires JDK 21+
./gradlew clean build

# Run the showcase
./gradlew :ferrox-java-showcase:bootRun
```

### 6.2 Modifying the Hexagon
1. **Rule #1**: Never put `@Entity`, `@Table`, or `@RestController` inside the Domain package.
2. If the domain needs external data, create `UserRepositoryPort` in the domain.
3. In the infrastructure package, create `PostgresUserRepositoryAdapter implements UserRepositoryPort`.

### 6.3 Managing Off-Heap Buffers
If you allocate an off-heap buffer using `ferrox-java-offheap`, **you must release it**. 
The JVM will not clean it up. If you lose the reference, you leak RAM until the OS OOM-killer terminates the process. Always wrap usage in `try-with-resources`.

---

## 7. User & DevOps Handbook (Operations)

### 7.1 JVM & Kernel Tuning
Deployment requires specific JVM flags to match hardware topology:
```bash
java -server \
     -XX:+UseZGC -XX:ZAllocationSpikeTolerance=5 \
     -Xms2G -Xmx2G \
     -XX:MaxDirectMemorySize=16G \
     -XX:+AlwaysPreTouch \
     -XX:+UseNUMA \
     -jar ferrox-java-showcase.jar
```
- `-XX:MaxDirectMemorySize=16G`: Allows massive off-heap mapping.
- `-Xmx2G`: Caps the actual JVM heap to force GC to stay extremely fast.
- `-XX:+AlwaysPreTouch`: Forces Linux to physically allocate memory pages at startup, preventing page-fault latency spikes during market hours.
- `-XX:+UseNUMA`: Pins thread memory allocations to local CPU sockets, drastically reducing QPI cross-socket latency.

### 7.2 Tracing & Observability
- Integrates **Micrometer** and **OpenTelemetry**.
- Trace IDs are propagated from the HTTP ingress, through the Hexagon, into the Event Sourcing persistence layer, providing end-to-end visibility in Jaeger/Datadog.

---

## 8. Senior Engineering Conclusion

Enterprise architecture is the absolute control of state, memory, and CPU execution paths. By moving high-throughput structures Off-Heap, leveraging zero-copy kernel syscalls, and enforcing mathematically pure Domain boundaries, we created a Java system that operates with the mechanical predictability of C++ while harnessing the vast JVM ecosystem. This is engineering for extreme scale.
