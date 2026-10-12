package standart.code.service;

import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.SenderAccountResponseDTO;
import standart.code.model.SenderAccount;
import standart.code.repository.SenderAccountRepository;

import java.util.List;

public interface SenderAccountServiceInterface {

    SenderAccountResponseDTO registerAccount(RegisterSenderRequest request);

    SenderAccountResponseDTO getById (Long id);

    List<SenderAccountResponseDTO> getAll();

    void deleteById (Long id);
}
