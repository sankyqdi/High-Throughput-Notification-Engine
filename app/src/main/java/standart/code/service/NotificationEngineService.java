package standart.code.service;

import com.sun.jdi.ThreadReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import standart.code.dto.TaskDTO;
import standart.code.error.ServerError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
public class NotificationEngineService {

    private final AtomicLong processCount = new AtomicLong(0);

    private Integer queueSize;

    private final BlockingQueue<TaskDTO> taskQueue = new ArrayBlockingQueue<>(5000);

    private ExecutorService workerPool;

    private final int threadCount = 10;

    private final Integer queueCopacity = 5000;

    private final AtomicLong errorsCount = new AtomicLong(0);

    private volatile boolean isShutdown = false;

    public boolean addTaskInPull(TaskDTO task) {

        if(isShutdown) {

            return false;

        }

        return taskQueue.offer(task);

    }

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

            if (taskDTO.getId() > 0) {

                throw new ServerError();

            }

            Thread.sleep(50);

            processCount.incrementAndGet();

        } catch (ServerError e) {

            log.info("Обработка задачи {} не прошла, ошибка {}", taskDTO.getId(), e.getMessage());
            errorsCount.incrementAndGet();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

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



}
