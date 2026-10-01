package standart.code.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import standart.code.dto.TaskDTO;
import standart.code.error.ServerError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEngineService {

    private final YandexEmailService emailService;

    private final AtomicLong processCount = new AtomicLong(0);

    private final BlockingQueue<TaskDTO> taskQueue = new ArrayBlockingQueue<>(5000);

    private ExecutorService workerPool;

    private final int threadCount = 10;

    private final Integer queueCapacity = 5000;

    private final AtomicLong errorsCount = new AtomicLong(0);

    private volatile boolean isShutdown = false;

    public boolean addTaskInPull(TaskDTO task) {

        if(isShutdown) {

            return false;

        }

        return taskQueue.offer(task);

    }

    @PostConstruct
    public void init() {

        this.workerPool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {

            workerPool.submit(this::processQueueLoop);

        }


    }

    private void processQueueLoop() {

        while (!Thread.currentThread().isInterrupted()) {

            try {

                TaskDTO task = taskQueue.take();

                processing(task);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                break;

            } catch (Throwable t) {

                log.error("Непредвиденная ошибка -> {}", t.getMessage());
                errorsCount.incrementAndGet();

            }
        }
    }

    @PreDestroy
    public void shutdown() {

        this.isShutdown = true;

        if (workerPool != null) {

            workerPool.shutdown();
            try{

                if(!workerPool.awaitTermination(2, TimeUnit.SECONDS)) {

                    workerPool.shutdownNow();

                }

            } catch (InterruptedException e) {

                workerPool.shutdownNow();
                Thread.currentThread().interrupt();

            }

        }

    }

    private void processing(TaskDTO taskDTO) {

        try {
            emailService.sendEmail(taskDTO.getEmail(), taskDTO.getBody(), taskDTO.getUserType());

            processCount.incrementAndGet();

            log.info("Задача \" {} \" успешно обработана и отправлена", taskDTO.getName());

        } catch (Exception e) {

            log.error("Сообщение {} не отправлено. {}", taskDTO.getName(), e.getMessage());

        }

    }

    public long getProcessCount() {

        return processCount.get();

    }

    public long getErrorsCount() {

        return errorsCount.get();

    }

    public  long getQueueSize() {

        return taskQueue.size();

    }


    public Integer getQueueCopacity() {
        return queueCapacity;
    }
}
