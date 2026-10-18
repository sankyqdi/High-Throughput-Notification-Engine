package standart.code.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import standart.code.config.MailConfig;
import standart.code.model.SenderAccount;
import util.AesCryptoUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexEmailService {

    private final MailConfig mailConfig;
    private final Map<String, JavaMailSender> senderCache = new ConcurrentHashMap<>();

    public void sendEmail(String senderEmail, String body, SenderAccount senderAccount) {

        try {
            JavaMailSender mailSender = senderCache.computeIfAbsent(senderAccount.getEmail(), username -> {

                String rawPassword = AesCryptoUtil.decrypt(senderAccount.getEncryptedPassword());
                return mailConfig.createMail(username, rawPassword);


            });

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAccount.getEmail());
            message.setTo(senderEmail);
            message.setSubject("Новое уведомление");
            message.setText(body);

            mailSender.send(message);

            log.info("Сообщение успешно отправлено на {}", senderEmail);

        }catch (Exception e) {

            log.error("Ошибка при отправки письма {}: {}", senderEmail, e.getMessage());
            throw e;

        }

    }

    public void clearCacheForUser(String email) {

        if(email != null) {

            JavaMailSender removeSender = senderCache.remove(email);

            if (removeSender != null) {

                log.info("Кеш JavaMailSender успешно очищен для email: {}", email);

            } else {

                log.debug("Кеш для email: {} отсутствует в памяти", email);

            }

        }



    }
}
