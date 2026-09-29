package standart.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import standart.code.model.User;

@Data
@Builder
public class NotificationDTO {

    @NotNull(message = "отправитель обязателен")
    private User user;

    @NotNull(message = "почта обязательна")
    private String email;

    @NotNull(message = "сообщение обязательно")
    @Size(max = 1000, message = "длина должна не превышать 1000 символов")
    private String message;

}
