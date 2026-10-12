package standart.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
        @NotBlank(message = "текущий пароль обязателен") String currentPassword,
        @NotBlank(message = "новый пароль обязателен")  @Size(min = 8, max = 64, message = "Новый пароль должен состоять от 9 до 64 символов")
        String newPassword
){
}
