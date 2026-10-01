package standart.code.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import standart.code.config.MailConfig;
import standart.code.model.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexEmailService {

    private final MailConfig mailConfig;

    public void sendEmail(String email, String body, User userType) {

        try {
            JavaMailSender mailSender = mailConfig.createMail(userType.getUsername(), userType.getPassword());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(userType.getUsername());
            message.setTo(email);
            message.setSubject("Новое уведомление");
            message.setText(body);

            mailSender.send(message);

            log.info("Сообщение успешно отправлено на {}", email);

        }catch (Exception e) {

            log.error("Ошибка при отправки письма {}: {}", email, e.getMessage());
            throw e;

        }

    }
}
