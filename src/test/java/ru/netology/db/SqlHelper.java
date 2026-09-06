package ru.netology.db;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/app";
    private static final String DB_USER = "app";
    private static final String DB_PASS = "pass";

    private static final QueryRunner runner = new QueryRunner();

    private SqlHelper() {
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Получить последний код подтверждения для пользователя по логину
    public static String getVerificationCode(String login) {
        String sql = "SELECT ac.code FROM auth_codes ac " +
                "JOIN users u ON ac.user_id = u.id " +
                "WHERE u.login = ? " +
                "ORDER BY ac.created DESC LIMIT 1";
        ResultSetHandler<String> handler = rs -> {
            if (!rs.next()) return null;
            return rs.getString("code");
        };
        try (Connection conn = getConnection()) {
            return runner.query(conn, sql, handler, login);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get verification code", e);
        }
    }

    // Очистить таблицу кодов (чтобы не накапливались между тестами)
    public static void clearAuthCodes() {
        try (Connection conn = getConnection()) {
            runner.update(conn, "DELETE FROM auth_codes");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear auth_codes", e);
        }
    }

    // Полная очистка БД (для полного сброса состояния)
    public static void cleanDatabase() {
        try (Connection conn = getConnection()) {
            runner.update(conn, "DELETE FROM card_transactions");
            runner.update(conn, "DELETE FROM auth_codes");
            runner.update(conn, "DELETE FROM cards");
            runner.update(conn, "DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean database", e);
        }
    }

    // Проверить, заблокирован ли пользователь по логину (status = 'blocked')
    public static boolean isUserBlocked(String login) {
        String sql = "SELECT status FROM users WHERE login = ?";
        ScalarHandler<String> handler = new ScalarHandler<>();
        try (Connection conn = getConnection()) {
            String status = runner.query(conn, sql, handler, login);
            return "blocked".equalsIgnoreCase(status);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user status", e);
        }
    }
}