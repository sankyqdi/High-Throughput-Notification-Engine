Notification Engine Service 🚀

High-performance asynchronous email notification service. Built with Java 21 and Spring Boot 3, it uses a custom multi-threaded in-memory processing engine (ExecutorService + ArrayBlockingQueue) to smooth out traffic spikes and process outbound emails asynchronously without blocking the main HTTP thread.
🛠 Tech Stack

    Core: Java 21, Spring Boot 3.3.4

    Data & Persistence: Spring Data JPA, PostgreSQL

    Concurrency: Java ExecutorService (Fixed Thread Pool), ArrayBlockingQueue

    Validation & DTO Mapping: Spring Validation, MapStruct 1.6.3, Lombok

    Logging: Logback Classic 1.5.18

    Testing: JUnit 5, Awaitility 4.2.2 (for testing asynchronous pipelines)

    Build System: Gradle (Kotlin DSL)

🏗 Architecture & Core Workflow

    REST Controller receives notification requests (single or bulk dispatches) and immediately returns an HTTP 202 Accepted response.

    In-Memory Engine (NotificationEngineService) accepts task objects (TaskDTO), normalizes and hashes sensitive customer data (EmailHasher), and places them into a thread-safe bounded ArrayBlockingQueue.

    Worker Pool (10 thread workers) asynchronously dequeues tasks from memory and executes physical email delivery to target recipients.

    Persistence Layer logs processing statuses, execution history, and engine metrics (EngineStats) into PostgreSQL.

