package standart.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterSenderRequest {

    @NotBlank(message = "email обязателен")
    @Email(message = "некорректный формат почты")
    private String email;

    @NotBlank(message = "пароль приложения обязателен")
    private String appPassword;

    private String host = "smtp.yandex.ru";

    private int port = 456;

}
