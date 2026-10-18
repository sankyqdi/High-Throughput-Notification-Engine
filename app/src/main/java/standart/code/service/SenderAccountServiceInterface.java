package standart.code.service;

import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.SenderAccountResponseDTO;
import standart.code.model.SenderAccount;
import standart.code.model.User;
import standart.code.repository.SenderAccountRepository;

import java.util.List;

public interface SenderAccountServiceInterface {

    SenderAccountResponseDTO registerAccount(RegisterSenderRequest request, User owner);

    SenderAccountResponseDTO getById (Long id, User owner);

    List<SenderAccountResponseDTO> getAll(User owner);

    void deleteById (Long id, User owner);
}
