import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.*;

import java.io.*;
import java.util.*;

class Encryption {

    private static final String ALGO = "AES";
    private static final byte[] keyValue
            = new byte[] {'Z', '4', 'e', 't', 'e', '_', 't',
                'S', '-', '!', '2', '%', 't', 'K', 'l', ';'};

    public static String encrypt(String Data) {
        try {
            // Генерира се ключ
            Key key = new SecretKeySpec(keyValue, ALGO);

            // Създаваме шифър за нашия алгоритъм
            Cipher cipher = Cipher.getInstance(ALGO);

            // Инициализираме шифъра за криптиране
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Криптираме подадения низ
            byte[] encVal = cipher.doFinal(Data.getBytes());

            // Конвертираме байтовете до стринг
            String encryptedValue = new BASE64Encoder().encode(encVal);

            return encryptedValue;
        }
        catch (Exception e) {
            System.out.println("Encryption error: " + e);
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            Key key = new SecretKeySpec(keyValue, ALGO);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedData);
            byte[] decValue = cipher.doFinal(decodedValue);
            String decryptedValue = new String(decValue);

            return decryptedValue;
        }
        catch (Exception e) {
            System.out.println("Decryption error: " + e);
            return null;
        }
    }
}