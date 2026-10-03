package standart.code.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import standart.code.dto.*;
import standart.code.error.QueueOverflow;
import standart.code.mapper.NotificationMapper;
import standart.code.model.Notification;
import standart.code.repository.NotificationRepository;
import util.EmailHasher;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService implements NotificationServiceInterface{

    private final NotificationEngineService notificationEngineService;

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;

    private final EngineStatsService engineStatsService;

    public EngineStatsDTO dataAcquisition(NotificationRequest notificationRequest) {

        int count = 0;

        for (var email : notificationRequest.getEmails()) {

            String hashedEmail = EmailHasher.hashEmailSha256(email);

            NotificationCreatedDTO notificationCreatedDTO = NotificationCreatedDTO.builder()
                    .userType(notificationRequest.getUserType())
                    .email(hashedEmail)
                    .message(notificationRequest.getMessage())
                    .build();

            create(notificationCreatedDTO);

            TaskDTO taskDTO = TaskDTO.builder()
                    .userType(notificationRequest.getUserType())
                    .email(email)
                    .name("message" + (count + 1))
                    .body(notificationRequest.getMessage())
                    .build();

            boolean added = notificationEngineService.addTaskInPull(taskDTO);

            if (!added) {

                throw new  QueueOverflow("Очередь переполнена");

            } else {

                count++;

            }
        }
        EngineStatsRequest engineStatsRequest = EngineStatsRequest.builder()
                .errorsCount(notificationEngineService.getErrorsCount())
                .processCount(notificationEngineService.getProcessCount())
                .queueCapacity(notificationEngineService.getQueueCopacity())
                .build();

        return engineStatsService.create(engineStatsRequest);
    }

    @Override
    public NotificationDTO create(NotificationCreatedDTO notificationCreatedDTO) {

        Notification notification = notificationMapper.map(notificationCreatedDTO);

        Notification savedNotification = notificationRepository.save(notification);

        return notificationMapper.map(savedNotification);

    }

    @Override
    public List<NotificationDTO> getAll() {

        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::map)
                .toList();

    }

    @Override
    public NotificationDTO getById(Long id) {

       Notification notification = notificationRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

       return notificationMapper.map(notification);

    }

    @Override
    public void delete(Long id) {

        Notification notification = notificationRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        notificationRepository.delete(notification);

    }

}
