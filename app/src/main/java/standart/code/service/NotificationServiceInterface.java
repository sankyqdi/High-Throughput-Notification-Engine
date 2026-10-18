package standart.code.service;

import standart.code.dto.EngineStatsDTO;
import standart.code.dto.NotificationCreatedDTO;
import standart.code.dto.NotificationDTO;
import standart.code.dto.NotificationRequest;
import standart.code.model.User;

import java.util.List;

public interface NotificationServiceInterface {

    EngineStatsDTO dataAcquisition(NotificationRequest notificationRequest, User user);

    NotificationDTO create(NotificationCreatedDTO notificationCreatedDTO);

    List<NotificationDTO> getAll();

    NotificationDTO getById(Long id);

    void delete(Long id);

}
