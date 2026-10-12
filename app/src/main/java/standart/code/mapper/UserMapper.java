package standart.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;
import standart.code.model.User;

@Mapper(componentModel =  "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", source = "request.email")
    @Mapping(target = "password", source = "passwordHash")
    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "locked", constant = "false")
    User map (RegisterUserRequest request, String passwordHash);

    @Mapping(target = "email", source = "username")
    UserResponseDTO map (User user);

}
