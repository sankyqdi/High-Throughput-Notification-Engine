# ⚡ High-Throughput Notification Engine

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React 19" />
  <img src="https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/Render-Deployed-46E3B7?style=for-the-badge&logo=render&logoColor=black" alt="Render" />
</p>

<p align="center">
  <b>Высоконагруженный асинхронный сервис пакетной отправки email-уведомлений с кастомным пулом воркеров, динамическим SMTP-контуром, шифрованием секретов и встроенной панелью телеметрии.</b>
</p>

---

## 📌 О проекте

При классической синхронной отправке писем через SMTP HTTP-потоки приложения блокируются ожиданием сетевого ответа внешнего почтового сервера (латентность 500–2500 мс на запрос). При массовой рассылке это приводит к быстрому исчерпанию пула потоков веб-сервера (Tomcat thread starvation) и падению API.

**High-Throughput Notification Engine** решает эту проблему за счёт изоляции приёма запросов от их фактической доставки:
* Входящие запросы валидируются, сохраняются и мгновенно буферизируются в неблокирующей очереди `ArrayBlockingQueue(5000)`.
* Фиксированный пул из 10 воркеров (`ExecutorService`) параллельно выгребает задачи и доставляет их адресатам без задержек для клиента.
* Пароли почтовых ящиков шифруются на уровне приложения алгоритмом AES, а email-адреса получателей хешируются через SHA-256 перед сохранением в историю.
* Весь сервис поставляется как единый артефакт (Single-Artifact Deployment), где React SPA встроен в статические ресурсы Spring Boot.

---

## 🛠 Технологический стек

* **Бэкенд:** Java 21, Spring Boot 3.3.4 (Spring Web, Spring Data JPA, Spring Security, Spring Mail).
* **Многопоточность:** Java Concurrency Utilities (`ExecutorService`, `ArrayBlockingQueue`, `ConcurrentHashMap`, `AtomicLong`).
* **База данных:** PostgreSQL, HikariCP.
* **Безопасность и криптография:** JWT (jjwt 0.12.6), BCrypt, AES-128 (Cipher), SHA-256 (`MessageDigest`).
* **Фронтенд:** React 19, Vite, Recharts, Lucide React.
* **Инфраструктура & Сборка:** Gradle, Docker (Multi-stage build), Cloud Host Render.

---

## 🏗 Архитектура системы

```mermaid
flowchart TD
    Client([React SPA / HTTP Client]) -->|POST /api/v1/notifications/send| Controller[NotificationController]
    
    subgraph Core ["Spring Boot Application"]
        Controller -->|1. Валидация & Auth| Service[NotificationService]
        Service -->|2. SHA-256 Hash| Hasher[EmailHasher]
        Service -->|3. Persist History| DB[(PostgreSQL)]
        Service -->|4. Non-blocking offer| Queue[ArrayBlockingQueue: 5000 задач]
        
        subgraph Workers ["Worker Thread Pool (10 Threads)"]
            Engine[NotificationEngineService] -->|take task| Queue
            Engine -->|Async Dispatch| EmailService[YandexEmailService]
            EmailService -->|AES Decrypt Password| AES[AesCryptoUtil]
            EmailService -->|Get/Compute Sender| Cache[ConcurrentHashMap Cache]
            Cache --> MailConfig[MailConfig]
        end
        
        Scheduler["@Scheduled (Every 5s)"] -->|Snapshot Metrics| Telemetry[Metric History Buffer]
    end

    EmailService -->|TLS / SSL: 465| SMTP[Yandex SMTP Server]
    Client -.->|GET /stats/history| Controller
    Controller -.->|Read Metrics| Telemetry
    
