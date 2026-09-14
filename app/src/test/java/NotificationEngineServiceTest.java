import org.junit.jupiter.api.Test;
import standart.code.dto.TaskDTO;
import standart.code.service.NotificationEngineService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationEngineServiceTest {

    @Test
    public void testNotificationEngineService() {

        NotificationEngineService notificationEngineService = new NotificationEngineService();

        assertEquals(0, notificationEngineService.getProcessCount());
        assertEquals(0, notificationEngineService.getQueueSize());
        assertEquals(5000, notificationEngineService.getQueueCopacity());

    }

    @Test
    public void testAddPullTask() {

        NotificationEngineService notificationEngineService = new NotificationEngineService();

        boolean checkout = notificationEngineService.addTaskInPull(new TaskDTO());

        assertTrue(checkout);

    }

    @Test
    public void testAddFullPullTask() {

        NotificationEngineService notificationEngineService = new NotificationEngineService();

        for (int i = 0; i < notificationEngineService.getQueueCopacity(); i++) {

            notificationEngineService.addTaskInPull(new TaskDTO());

        }

        assertEquals(5000, notificationEngineService.getQueueSize());

    }

    @Test
    public void testWorkingTaskInQueue() {

        NotificationEngineService service = new NotificationEngineService();

        service.init();

        boolean checkout = service.addTaskInPull(new TaskDTO());
        assertTrue(checkout);

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getProcessCount() == 1);

        assertEquals(0, service.getQueueSize());
        assertEquals(1, service.getProcessCount());

        service.shutdown();

    }

    @Test
    public  void testInsulationError() {

        NotificationEngineService notificationEngineService = new NotificationEngineService();

        TaskDTO taskDTO = new TaskDTO(0L);

        notificationEngineService.init();

        notificationEngineService.addTaskInPull(taskDTO);
        notificationEngineService.addTaskInPull(new TaskDTO(1L));

        assertEquals(1, notificationEngineService.getErrorsCount());
        assertEquals(1, notificationEngineService.getProcessCount());

    }

    @Test
    public void testCorrectStopService() {

        NotificationEngineService service = new NotificationEngineService();

        service.init();

        for (int i = 1; i < 20; i++) {

            service.addTaskInPull(new TaskDTO((long) i));

        }

        assertTimeoutPreemptively(Duration.ofSeconds(2), service::shutdown);

        assertEquals(19, service.getProcessCount() + service.getQueueSize() + service.getErrorsCount());

        boolean checkout = service.addTaskInPull(new TaskDTO(45L));
        assertFalse(checkout);

    }
}
