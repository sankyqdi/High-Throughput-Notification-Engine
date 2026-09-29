package standart.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import standart.code.model.User;

import java.util.List;

@Data
@Builder
public class NotificationRequest {

    @NotNull(message = "Отправитель обязателен")
    private User userType;

    @NotNull(message = "почта обязательна")
    @NotEmpty(message = "список не может быть пустым")
    @Email(message = "некорректная почта")
    private List<String> emails;

    @NotNull(message = "сообщение обязательно")
    @Size(max = 1000, message = "Длина должна не превышать 1000 символов")
    private String message;

}
