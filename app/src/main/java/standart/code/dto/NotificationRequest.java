package standart.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import standart.code.model.SenderAccount;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @NotNull(message = "ID аккаунта отправителя обязателен")
    private Long senderAccountId;

    @NotNull(message = "почта обязательна")
    @NotEmpty(message = "список не может быть пустым")
    private List<@Email(message = "некорректная почта")String> emails;

    @NotNull(message = "сообщение обязательно")
    @Size(max = 1000, message = "Длина должна не превышать 1000 символов")
    private String message;

}
