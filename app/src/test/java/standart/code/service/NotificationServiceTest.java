package standart.code.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.NotificationCreatedDTO;
import standart.code.dto.NotificationDTO;
import standart.code.dto.NotificationRequest;
import standart.code.mapper.NotificationMapper;
import standart.code.model.Notification;
import standart.code.model.SenderAccount;
import standart.code.model.User;
import standart.code.repository.NotificationRepository;
import standart.code.repository.SenderAccountRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса рассылки уведомлений NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationEngineService notificationEngineService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SenderAccountRepository senderAccountRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("dataAcquisition: успешная валидация владельца, сохранение в БД и постановка в очередь")
    void dataAcquisition_WhenValidRequestAndOwner_ShouldProcessSuccessfully() {
        User user = User.builder()
                .id(1L)
                .username("owner@mail.com")
                .build();

        SenderAccount sender = SenderAccount.builder()
                .id(10L)
                .email("sender@yandex.ru")
                .owner(user)
                .build();

        NotificationRequest request = NotificationRequest.builder()
                .senderAccountId(10L)
                .emails(List.of("client1@gmail.com", "client2@gmail.com", "client3@gmail.com"))
                .message("Test broadcast message")
                .build();

        when(senderAccountRepository.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sender));
        when(notificationMapper.map(any(NotificationCreatedDTO.class))).thenReturn(new Notification());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        when(notificationEngineService.addTaskToPool(any())).thenReturn(true);

        when(notificationEngineService.getErrorsCount()).thenReturn(0L);
        when(notificationEngineService.getProcessCount()).thenReturn(0L);
        when(notificationEngineService.getQueueCapacity()).thenReturn(5000);
        when(notificationEngineService.getQueueSize()).thenReturn(3L);

        EngineStatsDTO actualStats = notificationService.dataAcquisition(request, user);

        assertThat(actualStats.getAddedInThisBatch()).isEqualTo(3);
        assertThat(actualStats.getQueueCapacity()).isEqualTo(5000);
        assertThat(actualStats.getCurrentQueueSize()).isEqualTo(3L);

        verify(senderAccountRepository, times(1)).findByIdAndOwner(10L, user);
        verify(notificationRepository, times(3)).save(any(Notification.class));
        verify(notificationEngineService, times(3)).addTaskToPool(any());
    }

    @Test
    @DisplayName("dataAcquisition: выбрасывает EntityNotFoundException, если аккаунт отправителя не принадлежит пользователю")
    void dataAcquisition_WhenSenderNotFoundOrNotOwned_ShouldThrowException() {
        User user = User.builder()
                .id(1L)
                .username("owner@mail.com")
                .build();

        NotificationRequest request = NotificationRequest.builder()
                .senderAccountId(999L)
                .emails(List.of("client@mail.com"))
                .message("Hello")
                .build();

        when(senderAccountRepository.findByIdAndOwner(999L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.dataAcquisition(request, user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

        verify(notificationEngineService, never()).addTaskToPool(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: успешное маппирование и сохранение уведомления в БД")
    void create_WhenValidData_ShouldSaveAndReturnDto() {
        SenderAccount senderAccount = SenderAccount.builder()
                .id(1L)
                .email("sender@yandex.ru")
                .build();

        NotificationCreatedDTO requestDto = NotificationCreatedDTO.builder()
                .senderAccount(senderAccount)
                .email("recipient@gmail.com")
                .message("Test body")
                .build();

        Notification entity = Notification.builder()
                .id(1L)
                .senderAccount(senderAccount)
                .email("recipient@gmail.com")
                .message("Test body")
                .build();

        Notification savedEntity = Notification.builder()
                .id(100L)
                .senderAccount(senderAccount)
                .email("recipient@gmail.com")
                .message("Test body")
                .build();

        NotificationDTO expectedDto = NotificationDTO.builder()
                .sender(senderAccount)
                .email("recipient@gmail.com")
                .message("Test body")
                .build();

        when(notificationMapper.map(requestDto)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenReturn(savedEntity);
        when(notificationMapper.map(savedEntity)).thenReturn(expectedDto);

        NotificationDTO actualDto = notificationService.create(requestDto);

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(notificationRepository, times(1)).save(entity);
    }
}