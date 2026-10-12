package util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesCryptoUtil {

    private static final String ALGORITHM = "AES";

    private static final String SECRET_KEY = "MyForestLongSecretPonyAndSelly01";

    public static String encrypt(String plainText) {

        if (plainText == null) return null;

        try {

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {

            throw new RuntimeException("Ошибка шифрования пароля", e);

        }

    }

    public static String decrypt(String cipherText) {

        if (cipherText == null) return null;

        try {

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoderBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decoderBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);


        } catch (Exception e) {

            throw new RuntimeException("Ошибка расшифровки пароля", e);

        }

    }

}
