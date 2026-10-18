package standart.code.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.SenderAccountResponseDTO;
import standart.code.mapper.SenderAccountMapper;
import standart.code.model.SenderAccount;
import standart.code.model.User;
import standart.code.repository.SenderAccountRepository;
import standart.code.util.AesCryptoUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SenderAccountService implements SenderAccountServiceInterface {

    private final YandexEmailService emailService;

    private final SenderAccountMapper mapper;

    private final SenderAccountRepository senderAccountRepository;

    private final AesCryptoUtil aesCryptoUtil;

    @Override
    public SenderAccountResponseDTO registerAccount(RegisterSenderRequest request, User user) {
        String encryptedPassword = aesCryptoUtil.encrypt(request.getAppPassword());

        SenderAccount senderAccount = mapper.map(request, encryptedPassword);
        senderAccount.setOwner(user);

        SenderAccount savedAccount = senderAccountRepository.save(senderAccount);

        return mapper.map(savedAccount);

    }

    @Override
    public SenderAccountResponseDTO getById(Long id, User owner) {

        SenderAccount senderAccount = senderAccountRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapper.map(senderAccount);

    }

    @Override
    public List<SenderAccountResponseDTO> getAll(User owner) {

        return senderAccountRepository.findAllByOwner(owner).stream()
                .map(mapper::map)
                .toList();

    }

    @Override
    public void deleteById(Long id, User owner) {

         SenderAccount senderAccount = senderAccountRepository.findByIdAndOwner(id, owner)
                 .orElseThrow(() -> new EntityNotFoundException("Аккаунт с ID " + id + " не найден"));

         senderAccountRepository.delete(senderAccount);

         emailService.clearCacheForUser(senderAccount.getEmail());

    }
}
