package standart.code.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import standart.code.mapper.SenderAccountMapper;
import standart.code.model.SenderAccount;
import standart.code.model.User;
import standart.code.repository.SenderAccountRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SenderAccountServiceTest {

    @Mock
    private YandexEmailService emailService;

    @Mock
    private SenderAccountMapper mapper;

    @Mock
    private SenderAccountRepository repository;

    @InjectMocks
    private SenderAccountService service;

    @Test
    @DisplayName("Удаление аккаунта: успешное удаление и вызов сброса кеша")
    public void deleteById_Success() {
        User owner = User.builder().id(1L).username("owner@mail.com").build();
        SenderAccount account = SenderAccount.builder()
                .id(1L)
                .email("promo@yandex.ru")
                .owner(owner)
                .build();

        when(repository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(account));

        service.deleteById(1L, owner);

        verify(repository).delete(account);
        verify(emailService).clearCacheForUser("promo@yandex.ru");
    }

    @Test
    @DisplayName("Удаление аккаунта: исключение EntityNotFoundException, если ID не найден или чужой")
    public void deleteById_ThrowsException_WhenNotFound() {
        User owner = User.builder().id(1L).username("owner@mail.com").build();

        when(repository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(99L, owner))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).delete(any());
        verify(emailService, never()).clearCacheForUser(any());
    }
}
