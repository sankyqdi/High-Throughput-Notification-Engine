package standart.code.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EmailHasher {

    public static String hashEmailSha256(String rawEmail) {

        if (rawEmail == null) {

            return null;

        }

        String normalEmail = rawEmail.trim().toLowerCase();

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = digest.digest(normalEmail.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);

            for (byte b : encodedHash) {

                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {

                    hexString.append('0');

                }

                hexString.append(hex);

            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException("Ошибка хещирования: алгоритм SHA-256 не найден", e);

        }

    }

}
