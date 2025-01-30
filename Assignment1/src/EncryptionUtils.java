/**
 * EncryptionUtils provides AES encryption and decryption for secure data handling.
 * 
 * Features:
 * - Encrypts plaintext into a Base64-encoded string.
 * - Decrypts an encrypted string back to plaintext.
 * 
 * Note:
 * - The secret key is hardcoded for demonstration; use a secure key management system in production.
 * - AES requires a 16, 24, or 32-byte key.
 * 
 * @author Khanh Le
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtils {
    private static final String AES = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // Use a securely stored key

    public static String encrypt(String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] originalData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(originalData);
    }
}