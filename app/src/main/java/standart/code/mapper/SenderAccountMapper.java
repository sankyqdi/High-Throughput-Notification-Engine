package standart.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.SenderAccountResponseDTO;
import standart.code.model.SenderAccount;

@Mapper(componentModel = "string",
 unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SenderAccountMapper {

    SenderAccountResponseDTO map (SenderAccount senderAccount);

    @Mapping(target = "encryptedPassword", source = "encryptedPassword")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "id", ignore = true)
    SenderAccount map (RegisterSenderRequest request, String encryptedPassword);

}
