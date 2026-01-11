package passwordmanager.services;

import java.security.SecureRandom;

public class PasswordGenerator {

    /**
     * Generates a random password of specified length containing uppercase,
     * lowercase, digits, and special characters.
     * 
     * @param length
     * @return String
     */
    public static String generate(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        String allChars = upper + lower + digits + special;
        StringBuilder password = new StringBuilder();

        int newLength = length - 4; // At least one character from each category
        // Could add checks for minimum length here, not required at the moment though

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < newLength; i++) {
            int index = random.nextInt(allChars.length());
            password.append(allChars.charAt(index));
        }

        // Ensure at least one character from each category
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fisher-Yates shuffle
        char[] shuffledPassword = password.toString().toCharArray();
        shuffleArray(shuffledPassword);

        return new String(shuffledPassword);
    }

    /**
     * Shuffles the characters in the given array using the Fisher-Yates algorithm.
     * 
     * @param array
     */
    private static void shuffleArray(char[] array) {
        SecureRandom random = new SecureRandom();
        for (int i = array.length - 1; i > 0; i--) {
            int randomNumber = random.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[randomNumber];
            array[randomNumber] = temp;
        }
    }
}
