package standart.code.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import standart.code.dto.TaskDTO;
import standart.code.error.ServerError;

@Slf4j
@Getter
public class NotificationEngineService {

    private Integer processCount;

    private Integer queueSize;

    private final Integer queueCopacity = 5000;

    private Integer errorsCount;

    public boolean addTaskInPull(TaskDTO task) {

        processing(task);

    }

    public void init() {



    }

    public void shutdown() {



    }

    private void processing(TaskDTO taskDTO) {

        try {

            if (taskDTO.getId() == 0) {

                throw new ServerError();

            }

        } catch (ServerError e) {

            log.info("Обработка задачи {} не прошла, ошибка {}", taskDTO.getId(), e.getMessage());
            errorsCount++;

        }

    }

    private void executeTask(TaskDTO taskDTO) throws ServerError {


    }
}
