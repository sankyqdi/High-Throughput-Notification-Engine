package standart.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import standart.code.constant.ApiRoutes;
import standart.code.dto.RegisterSenderRequest;
import standart.code.model.User;
import standart.code.service.SenderAccountService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SenderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SenderAccountService senderAccountService;

    private User createMockUser() {
        return User.builder()
                .id(1L)
                .username("test_user@mail.com")
                .password("encoded_pass")
                .isEnabled(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/senders/register возвращает 200 OK при валидных данных")
    void registerSender_ValidRequest_ShouldReturnOk() throws Exception {
        User user = createMockUser();

        RegisterSenderRequest request = new RegisterSenderRequest();
        request.setEmail("sender@yandex.ru");
        request.setAppPassword("secret-app-password");
        request.setHost("smtp.yandex.ru");
        request.setPort(465);

        mockMvc.perform(post(ApiRoutes.API_REGISTER + "/register")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(senderAccountService).registerAccount(any(RegisterSenderRequest.class), any(User.class));
    }

    @Test
    @DisplayName("POST /api/v1/senders/register возвращает 400 Bad Request при невалидном email")
    void registerSender_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        User user = createMockUser();

        RegisterSenderRequest request = new RegisterSenderRequest();
        request.setEmail("invalid-email-format");
        request.setAppPassword("secret-pass");

        mockMvc.perform(post(ApiRoutes.API_REGISTER + "/register")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}