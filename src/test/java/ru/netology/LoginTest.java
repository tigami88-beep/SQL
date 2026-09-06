package ru.netology;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.db.SqlHelper;
import ru.netology.page.LoginPage;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты входа в систему")
class LoginTest {

    @BeforeAll
    static void setUpAll() {
        Configuration.headless = true;
        Configuration.timeout = 10000;
        Configuration.driverManagerEnabled = false;
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
    }

    @AfterEach
    void tearDown() {
        SqlHelper.clearAuthCodes();
        SqlHelper.unlockUser("vasya");
        closeWindow();
    }

    @Test
    @DisplayName("Успешный вход с валидными данными и кодом верификации из БД")
    void shouldLoginWithValidCredentialsAndVerificationCode() {
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getValidUser();
        var verificationPage = loginPage.validLogin(authInfo);

        String code = SqlHelper.getVerificationCode(authInfo.getLogin());
        assertNotNull(code, "Код верификации не найден в БД");

        var verificationCode = new DataHelper.VerificationCode(code);
        var dashboardPage = verificationPage.validVerify(verificationCode);

        assertTrue(dashboardPage.isDashboardVisible(),
                "Дашборд должен быть виден после успешного входа");
    }

    @Test
    @DisplayName("Ошибка при неверном пароле")
    void shouldShowErrorMessageOnWrongPassword() {
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getInvalidUser();
        loginPage.invalidLogin(authInfo);
        loginPage.verifyErrorMessageVisible();
    }

    @Test
    @DisplayName("Блокировка после трёх неверных попыток ввода пароля")
    void shouldBlockUserAfterThreeWrongAttempts() {
        var loginPage = new LoginPage();
        var authInfo = new DataHelper.AuthInfo("vasya", DataHelper.getInvalidPassword());

        loginPage.invalidLogin(authInfo);
        loginPage.verifyErrorMessageVisible();

        open("http://localhost:9999");
        loginPage = new LoginPage();
        loginPage.invalidLogin(authInfo);
        loginPage.verifyErrorMessageVisible();

        open("http://localhost:9999");
        loginPage = new LoginPage();
        loginPage.invalidLogin(authInfo);
        loginPage.verifyErrorMessageVisible();

        boolean isBlocked = SqlHelper.isUserBlocked("vasya");
        assertTrue(isBlocked,
                "Пользователь должен быть заблокирован после 3 неверных попыток");
    }
}