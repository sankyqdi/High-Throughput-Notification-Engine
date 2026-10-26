# High-Throughput Notification Engine

## Структура проекта

```text
app/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── standart/code/
    │   │   │   ├── Main.java
    │   │   │   ├── config/       # Настройка SMTP
    │   │   │   ├── constant/     # Константы API-маршрутов
    │   │   │   ├── controller/   # HTTP-контроллеры
    │   │   │   ├── dto/          # Запросы, ответы и задачи очереди
    │   │   │   ├── error/        # Ошибки приложения
    │   │   │   ├── mapper/       # Преобразование DTO и моделей
    │   │   │   ├── model/        # Сущности и модель отправителя
    │   │   │   ├── repository/   # Доступ к данным через Spring Data JPA
    │   │   │   └── service/      # Бизнес-логика и отправка уведомлений
    │   │   └── util/             # Хеширование email и шифрование паролей
    │   └── resources/
    └── test/
        └── java/
```

## Архитектура

Приложение построено на Spring Boot. Контроллеры принимают HTTP-запросы и передают работу сервисам. Сервисы координируют бизнес-логику, мапперы преобразуют DTO и модели, а репозитории обеспечивают сохранение сущностей.

### Основной поток уведомления

1. `NotificationController` принимает `NotificationRequest` и вызывает `NotificationService`.
2. `NotificationService` хеширует email для записи в историю и сохраняет уведомление через `NotificationRepository`.
3. Для отправки создаётся `TaskDTO` с исходным email, текстом сообщения и данными `User`; задача добавляется в ограниченную очередь `NotificationEngineService` ёмкостью 5000 задач.
4. Пул из 10 потоков забирает задачи из очереди и передаёт их в `YandexEmailService`.
5. `YandexEmailService` создаёт и кэширует `JavaMailSender` по аккаунту, получает пароль через `AesCryptoUtil` и использует SMTP-настройки `MailConfig`.
6. `NotificationService` возвращает показатели очереди и обработки. `NotificationController` также предоставляет текущие показатели работы движка.

```mermaid
flowchart LR
    Client[HTTP-клиент] --> NC[NotificationController]
    NC --> NS[NotificationService]
    NS -->|хеширует email| EH[EmailHasher]
    NS -->|DTO -> модель| NM[NotificationMapper]
    NM --> NR[NotificationRepository]
    NR --> N[(Notification)]
    NS -->|TaskDTO с исходным email| NE[NotificationEngineService]
    NE --> Q[(Ограниченная очередь)]
    Q --> W[Пул из 10 рабочих потоков]
    W -->|отправка задачи| YE[YandexEmailService]
    YE -->|SMTP sender| MC[MailConfig]
    YE -->|расшифровка пароля| AES[AesCryptoUtil]
    MC --> SMTP[SMTP-сервер Яндекса]
    YE --> SMTP
    NE -->|счётчики и размер очереди| NS
    NE -->|текущие показатели| NC
```

### Связи основных классов

Стрелки показывают использование или преобразование данных. Это логические зависимости приложения, а не обязательно связи между таблицами базы данных.

```mermaid
flowchart TB
    Main[Main] --> Spring[Spring Boot]

    subgraph API[HTTP-слой]
        NC[NotificationController]
        SAC[SenderAccountController]
    end

    subgraph Services[Сервисный слой]
        NS[NotificationService]
        SAS[SenderAccountService]
        NES[NotificationEngineService]
        ESS[EngineStatsService]
        YES[YandexEmailService]
    end

    subgraph Data[Слой данных]
        NR[NotificationRepository]
        SAR[SenderAccountRepository]
        ESR[EngineStatsRepository]
        N[Notification]
        SA[SenderAccount]
        ES[EngineStats]
    end

    subgraph Conversion[DTO и преобразования]
        NDTO[NotificationRequest / NotificationDTO / NotificationCreatedDTO]
        TDTO[TaskDTO]
        SDTO[RegisterSenderRequest / SenderAccountResponseDTO]
        EDTO[EngineStatsRequest / EngineStatsDTO]
        NM[NotificationMapper]
        SAM[SenderAccountMapper]
        ESM[EngineStatsMapper]
    end

    NC --> NS
    NC --> NES
    NS --> NES
    NS --> NR
    NS --> NM
    NS --> NDTO
    NS --> TDTO
    NS -. внедрён, сейчас не используется .-> ESS
    NR --> N
    NM --> N
    NM --> NDTO

    SAC --> SAS
    SAS --> SAR
    SAS --> SAM
    SAS --> YES
    SAR --> SA
    SAM --> SA
    SAM --> SDTO

    NES --> TDTO
    NES --> YES
    YES --> MailConfig[MailConfig]
    YES --> User[User]
    YES --> AES[AesCryptoUtil]

    ESS --> ESR
    ESS --> ESM
    ESR --> ES
    ESM --> ES
    ESM --> EDTO

    NS --> EmailHasher[EmailHasher]
```

### Роли компонентов

| Компонент | Назначение |
|---|---|
| `NotificationController` | Приём уведомлений и выдача текущей статистики движка. |
| `NotificationService` | Сохранение уведомлений, хеширование адресов для истории, постановка задач в очередь. |
| `NotificationEngineService` | Ограниченная очередь, пул обработчиков и счётчики успешно обработанных задач и ошибок очереди. |
| `YandexEmailService` | Формирование письма, кэширование отправителей и отправка через SMTP. |
| `SenderAccountController`, `SenderAccountService` | Регистрация и управление сохранёнными аккаунтами отправителей. |
| `EngineStatsService` | Создание, чтение и удаление сохранённых записей статистики. |
| `*Repository` | CRUD-доступ к соответствующим JPA-моделям. |
| `*Mapper` | Преобразование между DTO и моделями. |
| `EmailHasher`, `AesCryptoUtil` | SHA-256-хеширование email для истории и шифрование/расшифровка пароля аккаунта. |

`SenderAccount` — отдельная сохраняемая модель аккаунта отправителя. В текущем потоке постановки уведомлений данные отправителя для письма поступают из `User` внутри запроса и `TaskDTO`; `SenderAccountService` отдельно управляет зарегистрированными аккаунтами. `EngineStatsService` также является отдельным сервисом хранения статистики, тогда как live-статистика в контроллере читается из счётчиков `NotificationEngineService`.
