package standart.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import standart.code.dto.NotificationCreatedDTO;
import standart.code.dto.NotificationDTO;
import standart.code.model.Notification;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationDTO map(Notification notification);

    Notification map(NotificationCreatedDTO notificationCreatedDTO);


}
