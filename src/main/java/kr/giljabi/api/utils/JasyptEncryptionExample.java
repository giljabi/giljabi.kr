package kr.giljabi.api.utils;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class JasyptEncryptionExample {
    public static void main(String[] args) {
        // 암호화 키 설정
        String encryptionKey = args[0]; // 여기에 사용할 암호화 키를 입력하세요.
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey); // 암호화 키 설정

        String encryptedText = textEncryptor.encrypt("jdbc:postgresql://localhost:5432/helloworlddb?currentSchema=public&useSSL=false");
        System.out.println("url: " + encryptedText);

        encryptedText = textEncryptor.encrypt("hello");
        System.out.println("username: " + encryptedText);

        encryptedText = textEncryptor.encrypt("world");
        System.out.println("password: " + encryptedText);

        encryptedText = textEncryptor.encrypt("accessKey");
        System.out.println("accessKey: " + encryptedText);

        encryptedText = textEncryptor.encrypt("secretKey");
        System.out.println("secretKey: " + encryptedText);


        // 복호화 예제 (테스트용)
/*        String decryptedText = textEncryptor.decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);*/

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("yourpassword");
        System.out.println("encodedPassword: " + encodedPassword);

    }
}



