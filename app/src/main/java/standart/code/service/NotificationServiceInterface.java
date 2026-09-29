package standart.code.service;

import standart.code.dto.NotificationCreatedDTO;
import standart.code.dto.NotificationDTO;

import java.util.List;

public interface NotificationServiceInterface {

    NotificationDTO create(NotificationCreatedDTO notificationCreatedDTO);

    List<NotificationDTO> getAll();

    NotificationDTO getById(Long id);

    void delete(Long id);

}
