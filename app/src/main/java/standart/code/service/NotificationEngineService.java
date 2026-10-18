package standart.code.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.TaskDTO;

import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEngineService {

    private final YandexEmailService emailService;

    private final AtomicLong processCount = new AtomicLong(0);
    private final AtomicLong errorsCount = new AtomicLong(0);

    private final BlockingQueue<TaskDTO> taskQueue = new ArrayBlockingQueue<>(5000);
    private final Integer queueCapacity = 5000;
    private final int threadCount = 10;

    private ExecutorService workerPool;
    private volatile boolean isShutdown = false;

    private static final int MAX_HISTORY_SIZE = 60;
    private final Deque<EngineStatsDTO> metricHistory = new ArrayDeque<>(MAX_HISTORY_SIZE);
    private long lastCapturedProcessCount = 0;

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

    @Scheduled(fixedRate = 5000)
    public synchronized void captureMetricsSnapshot() {

        long currentProcessed = processCount.get();
        long diff = currentProcessed - lastCapturedProcessCount;
        lastCapturedProcessCount = currentProcessed;

        EngineStatsDTO snapshot = EngineStatsDTO.builder()
                .errorsCount(errorsCount.get())
                .processCount(processCount.get())
                .queueCapacity(queueCapacity)
                .currentQueueSize(taskQueue.size())
                .throughput(diff)
                .dateTime(OffsetDateTime.now())
                .build();

        if (metricHistory.size() >= MAX_HISTORY_SIZE) {

            metricHistory.pollFirst();

        }

        metricHistory.addLast(snapshot);

    }

    public synchronized List<EngineStatsDTO> getMetricsHistory() {

        return new ArrayList<>(metricHistory);

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
            emailService.sendEmail(taskDTO.getEmail(), taskDTO.getBody(), taskDTO.getSenderAccount());

            processCount.incrementAndGet();

            log.info("Задача \" {} \" успешно обработана и отправлена", taskDTO.getName());

        } catch (Exception e) {

            errorsCount.incrementAndGet();
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


    public Integer getQueueCapacity() {
        return queueCapacity;
    }
}
