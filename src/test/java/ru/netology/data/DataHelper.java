package ru.netology.data;

import com.github.javafaker.Faker;
import lombok.Value;
import java.util.Locale;

public class DataHelper {
    private static final Faker faker = new Faker(new Locale("en"));

    private DataHelper() {}

    @Value
    public static class AuthInfo {
        String login;
        String password;
    }

    @Value
    public static class VerificationCode {
        String code;
    }

    public static AuthInfo getValidUser() {
        return new AuthInfo("vasya", "qwerty123");
    }

    public static AuthInfo getInvalidUser() {
        return new AuthInfo("vasya", getInvalidPassword());
    }

    public static String getInvalidPassword() {
        return faker.internet().password();
    }
}
