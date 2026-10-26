package standart.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import standart.code.model.SenderAccount;

@Data
@Builder
@AllArgsConstructor
public class NotificationCreatedDTO {

    @NotNull(message = "Отправитель обязателен")
    private SenderAccount senderAccount;

    @NotNull(message = "почта обязательна")
    private String email;

    private String subject;

    @NotNull(message = "сообщение обязательно")
    @Size(max = 1000, message = "Длина должна не превышать 1000 символов")
    private String message;

}
