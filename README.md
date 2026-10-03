# Notification Engine Service 🚀

High-performance, scalable, asynchronous email notification engine. Built with **Java** and **Spring Boot**, this service features a custom multi-threaded processing pipeline (Producer-Consumer pattern via `ExecutorService` & `ArrayBlockingQueue`) paired with dynamic multi-account SMTP dispatch (Yandex/Gmail).

It completely decouples database persistence from mail transport, preventing external SMTP latency from blocking HTTP requests.

---

## 🛠 Tech Stack

- **Core & Runtime:** Java, Spring Boot
- **Database & Persistence:** Spring Data JPA, PostgreSQL, Liquibase
- **Concurrency & Async Pipeline:** Custom Worker Pool (`ExecutorService`), Bounded Queue (`ArrayBlockingQueue`), Thread Safety (`AtomicLong`, `ConcurrentHashMap`)
- **Mail Transport:** Spring Boot Starter Mail (`JavaMailSender`), Dynamic Multi-Account SMTP Configuration
- **Data Protection & Mapping:** SHA-256 Email Hashing (`EmailHasher`), MapStruct, Lombok
- **Logging & Monitoring:** Logback Classic, Real-time Engine Metrics (`EngineStatsDTO`)
- **Testing:** JUnit 5, Awaitility
- **Build System:** Gradle (Kotlin DSL)

---

1. **Batch Ingestion & Decoupling:** `NotificationService` accepts bulk notification requests, normalizes sensitive data by hashing email addresses via `EmailHasher` (SHA-256) for compliant database storage, and persists the notification[cite: 7].
2. **Task Enqueueing:** Unhashed dispatch tasks (`TaskDTO`) are offered to `NotificationEngineService`'s bounded `ArrayBlockingQueue`[cite: 7, 8].
3. **Multi-Threaded Consumer Pool:** A managed pool of worker threads continuously dequeues tasks without blocking client HTTP threads[cite: 8].
4. **Dynamic Multi-Account SMTP Delivery:** `YandexEmailService` dynamically instantiates and caches (`ConcurrentHashMap`) `JavaMailSender` instances for distinct user accounts, executing secure SMTP deliveries[cite: 6, 9].
5. **Real-time Observability:** Every batch operation returns an `EngineStatsDTO` snapshot containing live engine health metrics (processed count, total errors, queue capacity, and current queue utilization)[cite: 8].

---

## 🚀 Key Features

- **Decoupled Persistence & Dispatch:** Database logging uses hashed PII while the in-memory engine handles raw email addresses for delivery[cite: 7, 8].
- **Dynamic Multi-Account SMTP:** Supports sending emails from multiple different Yandex/Gmail credentials on the fly with built-in sender instance caching[cite: 6, 9].
- **Backpressure Protection:** Uses a bounded `ArrayBlockingQueue` (5000 tasks max) to smooth out traffic spikes and protect memory usage[cite: 8].
- **Graceful Lifecycle Management:** Integrated with Spring's `@PostConstruct` and `@PreDestroy` for clean worker pool startup and graceful shutdown termination[cite: 8].
- **Instant Health Telemetry:** Provides immediate stats on queue load, throughput, and

