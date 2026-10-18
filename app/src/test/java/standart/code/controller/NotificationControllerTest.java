package standart.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import standart.code.constant.ApiRoutes;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.NotificationRequest;
import standart.code.model.User;
import standart.code.service.NotificationEngineService;
import standart.code.service.NotificationService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты контроллера NotificationController")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationEngineService notificationEngineService;

    private User createMockUser() {

        return User.builder()
                .id(1L)
                .username("test_user@mail.com")
                .password("encoded_pass")
                .isEnabled(true)
                .build();

    }

    @Test
    @DisplayName("POST /send: успешная отправка рассылки авторизованным пользователем (200 OK)")
    void sendNotification_WhenValidRequest_ShouldReturnOk() throws Exception {

        User user = createMockUser();

        NotificationRequest request = NotificationRequest.builder()
                .senderAccountId(10L)
                .emails(List.of("client1@gmail.com", "client2@gmail.com"))
                .message("Hello from test broadcast!")
                .build();

        EngineStatsDTO expectedStats = EngineStatsDTO.builder()
                .addedInThisBatch(2)
                .queueCapacity(5000)
                .currentQueueSize(2L)
                .processCount(0L)
                .errorsCount(0L)
                .dateTime(OffsetDateTime.now())
                .build();

        when(notificationService.dataAcquisition(any(NotificationRequest.class), any(User.class)))
                .thenReturn(expectedStats);

        mockMvc.perform(post(ApiRoutes.NOTIFICATIONS + ApiRoutes.SEND_NOTIFICATION)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addedInThisBatch").value(2))
                .andExpect(jsonPath("$.queueCapacity").value(5000))
                .andExpect(jsonPath("$.currentQueueSize").value(2));
    }

    @Test
    @DisplayName("POST /send: ошибка валидации при пустом списке адресатов (400 Bad Request)")
    void sendNotification_WhenEmailsEmpty_ShouldReturnBadRequest() throws Exception {

        User user = createMockUser();

        NotificationRequest request = NotificationRequest.builder()
                .senderAccountId(10L)
                .emails(Collections.emptyList())
                .message("Message")
                .build();

        mockMvc.perform(post(ApiRoutes.NOTIFICATIONS + ApiRoutes.SEND_NOTIFICATION)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.emails").exists());
    }

    @Test
    @DisplayName("POST /send: ошибка валидации при отсутствии senderAccountId (400 Bad Request)")
    void sendNotification_WhenSenderAccountIdNull_ShouldReturnBadRequest() throws Exception {

        User user = createMockUser();

        NotificationRequest request = NotificationRequest.builder()
                .senderAccountId(null) // Нарушение @NotNull
                .emails(List.of("valid@mail.com"))
                .message("Valid message")
                .build();

        mockMvc.perform(post(ApiRoutes.NOTIFICATIONS + ApiRoutes.SEND_NOTIFICATION)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.senderAccountId").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /stats: получение текущих метрик движка (200 OK)")
    void getLiveStats_ShouldReturnOk() throws Exception {

        when(notificationEngineService.getErrorsCount()).thenReturn(5L);
        when(notificationEngineService.getProcessCount()).thenReturn(150L);
        when(notificationEngineService.getQueueCapacity()).thenReturn(5000);
        when(notificationEngineService.getQueueSize()).thenReturn(12L);

        mockMvc.perform(get(ApiRoutes.NOTIFICATIONS + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorsCount").value(5))
                .andExpect(jsonPath("$.processCount").value(150))
                .andExpect(jsonPath("$.queueCapacity").value(5000))
                .andExpect(jsonPath("$.currentQueueSize").value(12))
                .andExpect(jsonPath("$.dateTime").isNotEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /stats/history: получение истории метрик движка (200 OK)")
    void getStatsHistory_ShouldReturnOk() throws Exception {

        EngineStatsDTO snapshot = EngineStatsDTO.builder()
                .currentQueueSize(0L)
                .processCount(100L)
                .errorsCount(2L)
                .queueCapacity(5000)
                .throughput(50L)
                .dateTime(OffsetDateTime.now())
                .build();

        when(notificationEngineService.getMetricsHistory()).thenReturn(List.of(snapshot));

        mockMvc.perform(get(ApiRoutes.NOTIFICATIONS + "/stats/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].throughput").value(50));

    }
}
