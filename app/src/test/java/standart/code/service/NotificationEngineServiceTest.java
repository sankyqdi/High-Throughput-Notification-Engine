package standart.code.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import standart.code.dto.TaskDTO;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование движка асинхронных уведомлений NotificationEngineService")
class NotificationEngineServiceTest {

    @Mock
    private YandexEmailService emailService;

    private NotificationEngineService service;

    @BeforeEach
    void setUp() {
        service = new NotificationEngineService(emailService);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("Проверка начального состояния: очередь и счётчики должны быть пустыми")
    void initialState_ShouldHaveZeroCountersAndDefaultCapacity() {
        assertEquals(0, service.getProcessCount());
        assertEquals(0, service.getQueueSize());
        assertEquals(0, service.getErrorsCount());
        assertEquals(5000, service.getQueueCapacity());
    }

    @Test
    @DisplayName("addTaskInPull() должен успешно добавлять задачу в непереполненную очередь")
    void addTaskInPull_WhenQueueIsNotFull_ShouldReturnTrue() {
        TaskDTO task = TaskDTO.builder()
                .email("test@mail.com")
                .name("Приветственное письмо")
                .body("Контент")
                .build();

        boolean isAdded = service.addTaskInPull(task);

        assertTrue(isAdded);
        assertEquals(1, service.getQueueSize());
    }

    @Test
    @DisplayName("addTaskInPull() должен возвращать false при попытке превысить емкость очереди")
    void addTaskInPull_WhenQueueIsFull_ShouldRejectExtraTask() {
        for (int i = 0; i < service.getQueueCapacity(); i++) {
            service.addTaskInPull(TaskDTO.builder().name("Task " + i).build());
        }

        assertEquals(5000, service.getQueueSize());

        boolean isExtraAdded = service.addTaskInPull(TaskDTO.builder().name("Overflow task").build());

        assertFalse(isExtraAdded);
        assertEquals(5000, service.getQueueSize());
    }

    @Test
    @DisplayName("Потоки воркера должны забирать задачу из очереди и успешно её обрабатывать")
    void processQueueLoop_ShouldProcessTaskSuccessfully() {
        service.init();

        TaskDTO task = TaskDTO.builder()
                .email("client@domain.com")
                .name("Order notification")
                .body("Order #123")
                .build();

        boolean isAdded = service.addTaskInPull(task);
        assertTrue(isAdded);

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getProcessCount() == 1);

        assertEquals(0, service.getQueueSize());
        assertEquals(1, service.getProcessCount());
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Ошибка в сервисе отправки email не должна останавливать обработку следующих задач")
    void processing_WhenEmailServiceFails_ShouldCatchExceptionAndContinue() {

        doThrow(new RuntimeException("SMTP connection refused"))
                .doNothing()
                .when(emailService).sendEmail(any(), any(), any());

        service.init();

        service.addTaskInPull(TaskDTO.builder().name("Faulty task").build());
        service.addTaskInPull(TaskDTO.builder().name("Normal task").build());

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getProcessCount() == 1);

        verify(emailService, times(2)).sendEmail(any(), any(), any());
        assertEquals(0, service.getQueueSize());
        assertEquals(1, service.getProcessCount());
    }

    @Test
    @DisplayName("shutdown() должен корректно останавливать пул и отклонять новые задачи")
    void shutdown_ShouldStopWorkerPoolAndRejectNewTasks() {
        service.init();

        for (int i = 1; i <= 20; i++) {
            service.addTaskInPull(TaskDTO.builder().name("Task " + i).build());
        }

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> service.shutdown());

        boolean isAddedAfterShutdown = service.addTaskInPull(TaskDTO.builder().name("Late task").build());
        assertFalse(isAddedAfterShutdown);

        assertEquals(20, service.getProcessCount() + service.getQueueSize());
    }
}