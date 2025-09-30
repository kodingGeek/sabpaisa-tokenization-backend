import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateEncryptionKey {
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        byte[] key = keyGen.generateKey().getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(key);
        
        System.out.println("===========================================");
        System.out.println("AES-256 Master Key Generated:");
        System.out.println(encodedKey);
        System.out.println("===========================================");
        System.out.println("\nAdd this to your application.yml:");
        System.out.println("app:");
        System.out.println("  encryption:");
        System.out.println("    master-key: " + encodedKey);
    }
}