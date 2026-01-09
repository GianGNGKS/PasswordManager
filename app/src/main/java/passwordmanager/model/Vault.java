package passwordmanager.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;

public class Vault {
    private static final int SALT_SIZE = 16; // bytes
    private static final int IV_SIZE = 12; // bytes for GCM
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String PATH = "vault.dat";

    /**
     * Saves the list of entries to a file, encrypting them with a key derived
     * from the master password.
     * First, it generates a random salt and IV.
     * Then, it derives a SecretKey from the master password and salt.
     * Next, it initializes a Cipher in encryption mode with the derived key and IV.
     * Finally, it writes the salt, IV, and encrypted entries to a file.
     * 
     * @param entries
     * @param masterPassword
     * @throws Exception
     */
    public void save(List<Entry> entries, String masterPassword) throws Exception {
        // 1. Generate salt & iv
        byte[] salt = generateRandom(SALT_SIZE);
        byte[] iv = generateRandom(IV_SIZE);
        // 2. Derive key
        SecretKey key = deriveKey(masterPassword, salt);
        // 3. Initialize cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // 4. Write to file
        writeToFile(salt, iv, cipher, entries);
        System.out.println("\t-> Vault saved successfully: " + PATH + "\n\t-> Number of entries: " + entries.size());
    }

    /**
     * Loads and decrypts the list of entries from a file using the provided master
     * password.
     * First, it reads the salt and IV from the beginning of the file.
     * Then, it derives a SecretKey from the master password and salt.
     * Next, it initializes a Cipher in decryption mode with the derived key and IV.
     * Finally, it reads and decrypts the entries from the file.
     * 
     * @param masterPassword
     * @return List<Entry>
     * @throws Exception
     */
    public List<Entry> load(String masterPassword) throws Exception {
        // 1. Read salt & iv from file
        try (FileInputStream fis = new FileInputStream(PATH);) {
            byte[] salt = new byte[SALT_SIZE];
            byte[] iv = new byte[IV_SIZE];
            if (fis.read(salt) != SALT_SIZE || fis.read(iv) != IV_SIZE) {
                throw new IOException("Archivo corrupto o incompleto");
            }
            // 2. Derive key
            SecretKey key = deriveKey(masterPassword, salt);
            // 3. Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            // 4. Read entries
            return readFromFile(fis, cipher);
        }
    }

    /**
     * Reads and decrypts the list of entries from a file using the provided Cipher.
     * 
     * @param cipher
     * @return List<Entry>
     * @throws Exception
     */
    private List<Entry> readFromFile(FileInputStream fis, Cipher cipher) throws Exception {
        try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                ObjectInputStream ois = new ObjectInputStream(cis);) {
            @SuppressWarnings("unchecked")
            List<Entry> entries = (List<Entry>) ois.readObject();
            return entries;
        }
    }

    /**
     * Writes the salt, IV, and encrypted entries to a file.
     * 
     * @param salt
     * @param iv
     * @param cipher
     * @param entries
     * @throws Exception
     */
    private void writeToFile(byte[] salt, byte[] iv, Cipher cipher, List<Entry> entries) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(PATH);) {
            // 4.1 Write salt & iv at the beginning
            fos.write(salt);
            fos.write(iv);
            // 4.2 Write encrypted data
            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                ObjectOutputStream oos = new ObjectOutputStream(cos);
                oos.writeObject(entries);
            }
        }
    }

    /**
     * Derives a SecretKey from a given password and salt using PBKDF2 with
     * HMAC-SHA256.
     * First, it creates a PBEKeySpec with the password, salt, iteration count, and
     * key length.
     * Then, it uses a SecretKeyFactory to generate the secret key. Finally, it
     * converts the generated key into an AES SecretKeySpec.
     * 
     * @param password
     * @param salt
     * @return SecretKey
     * @throws Exception
     */
    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        // 1. Pass + Salt + 65536 iterations + 256 bits
        int iterations = 65536;
        int bits = 256;
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, bits);

        // 2. Generate the key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // 3. Create the secret key
        SecretKey tmp = factory.generateSecret(spec);
        // 4. Convert to AES key
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Generates a random byte array of the specified length using SecureRandom, a
     * Java class that provides a cryptographically strong random number generator.
     * 
     * @param length The length of the byte array to generate.
     * @return A byte array filled with random bytes.
     */
    public byte[] generateRandom(int length) {
        byte[] bytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
