package standart.code.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import standart.code.dto.*;
import standart.code.error.QueueOverflow;
import standart.code.mapper.NotificationMapper;
import standart.code.model.Notification;
import standart.code.model.SenderAccount;
import standart.code.model.User;
import standart.code.repository.NotificationRepository;
import standart.code.repository.SenderAccountRepository;
import standart.code.util.EmailHasher;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService implements NotificationServiceInterface{

    private final NotificationEngineService notificationEngineService;

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;

    private final SenderAccountRepository senderAccountRepository;


    @Transactional
    @Override
    public EngineStatsDTO dataAcquisition(NotificationRequest notificationRequest, User user) {

        SenderAccount senderAccount = senderAccountRepository.findByIdAndOwner(notificationRequest.getSenderAccountId(), user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Аккаунт отправителя с ID " + notificationRequest.getSenderAccountId() + " не найден"));

        int count = 0;

        String validSubject = (notificationRequest.getSubject() != null && notificationRequest.getSubject().trim().isEmpty())
                ? notificationRequest.getSubject()
                : "Новое уведомление";

        for (var email : notificationRequest.getEmails()) {

            String hashedEmail = EmailHasher.hashEmailSha256(email);

            NotificationCreatedDTO notificationCreatedDTO = NotificationCreatedDTO.builder()
                    .senderAccount(senderAccount)
                    .email(hashedEmail)
                    .subject(validSubject)
                    .message(notificationRequest.getMessage())
                    .build();

            create(notificationCreatedDTO);

            TaskDTO taskDTO = TaskDTO.builder()
                    .senderAccount(senderAccount)
                    .email(email)
                    .name("message" + (count + 1))
                    .subject(validSubject)
                    .body(notificationRequest.getMessage())
                    .build();

            boolean added = notificationEngineService.addTaskToPool(taskDTO);
 
            if (!added) {

                throw new  QueueOverflow("Очередь переполнена");

            } else {

                count++;

            }
        }

        return EngineStatsDTO.builder()
                .errorsCount(notificationEngineService.getErrorsCount())
                .processCount(notificationEngineService.getProcessCount())
                .queueCapacity(notificationEngineService.getQueueCapacity())
                .currentQueueSize(notificationEngineService.getQueueSize())
                .addedInThisBatch(count)
                .build();
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
