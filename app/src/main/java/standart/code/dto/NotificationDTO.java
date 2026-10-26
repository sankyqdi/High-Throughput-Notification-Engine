package standart.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import standart.code.model.SenderAccount;

@Data
@Builder
public class NotificationDTO {

    @NotNull(message = "отправитель обязателен")
    private SenderAccount sender;

    @NotNull(message = "почта обязательна")
    private String email;

    private String subject;

    @NotNull(message = "сообщение обязательно")
    @Size(max = 1000, message = "длина должна не превышать 1000 символов")
    private String message;

}
