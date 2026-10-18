package standart.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import standart.code.dto.AuthRequest;
import standart.code.dto.AuthResponse;
import standart.code.dto.UserResponseDTO;
import standart.code.model.Role;
import standart.code.model.User;
import standart.code.service.AuthService;
import standart.code.service.JwtService;
import standart.code.service.UserServiceInterface;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserServiceInterface userService;

    @Test
    @DisplayName("POST /api/v1/auth/login открыт без авторизации и возвращает 200 OK")
    void login_ShouldBePermitAll() throws Exception {
        var request = new AuthRequest("test@mail.com", "password123");
        var response = new AuthResponse("mocked-jwt-token", "test@mail.com");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me без токена возвращает 403 Forbidden")
    void getCurrentProfile_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users/me с невалидным Bearer-токеном возвращает 403 Forbidden")
    void getCurrentProfile_WithInvalidToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid-garbage-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users/me с валидным JWT пропускает запрос и возвращает профиль 200 OK")
    void getCurrentProfile_WithValidToken_ShouldReturnOk() throws Exception {
        String email = "student@mail.com";
        User user = User.builder()
                .id(1L)
                .username(email)
                .password("encoded_pass")
                .role(Role.ROLE_USER)
                .isEnabled(true)
                .build();

        when(userService.loadUserByUsername(email)).thenReturn(user);
        when(userService.getCurrentProfile(email)).thenReturn(
                UserResponseDTO.builder()
                        .id(1L)
                        .email(email)
                        .role(Role.ROLE_USER)
                        .enabled(true)
                        .build()
        );

        String validToken = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(email));
    }
}
