package passwordmanager.services;

import org.junit.jupiter.api.Test;

public class PasswordGeneratorTest {
    @Test
    void validLength() {
        int length = 0;
        try {
            String password = PasswordGenerator.generate(length);
            System.out.println("Valid Length: Generated password: " + password);
            assert password.length() <= length;
        } catch (IllegalArgumentException e) {
            System.out.println("Valid Length: Caught expected exception for length " + length + ": " + e.getMessage());
        }
    }

    @Test
    void testLength() {
        String password = PasswordGenerator.generate(12);
        System.out.println("Length: Generated password: " + password);
        assert password.length() == 12;
    }

    @Test
    void testUppercase() {
        String password = PasswordGenerator.generate(20);
        System.out.println("Uppercase: Generated password: " + password);
        assert password.chars().anyMatch(Character::isUpperCase);
    }

    @Test
    void testLowercase() {
        String password = PasswordGenerator.generate(20);
        System.out.println("Lowercase: Generated password: " + password);
        assert password.chars().anyMatch(Character::isLowerCase);
    }

    @Test
    void testDigits() {
        String password = PasswordGenerator.generate(20);
        System.out.println("Digits: Generated password: " + password);
        assert password.chars().anyMatch(Character::isDigit);
    }

    @Test
    void testSpecialCharacters() {
        String password = PasswordGenerator.generate(20);
        System.out.println("Special Characters: Generated password: " + password);
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        assert password.chars().anyMatch(ch -> specialChars.indexOf(ch) >= 0);
    }

    @Test
    void uniquePasswords() {
        String password1 = PasswordGenerator.generate(16);
        String password2 = PasswordGenerator.generate(16);
        System.out.println("Unique Passwords: Generated passwords: " + password1 + " , " + password2);
        assert !password1.equals(password2);
    }
}
