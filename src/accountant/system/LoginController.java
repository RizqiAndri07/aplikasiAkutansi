package accountant.system;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label registerLabel;
    @FXML
    private Label forgotPasswordLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private SessionManager sessionManager;
    @FXML
    private StackPane pane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sessionManager = SessionManager.getInstance();

        Platform.runLater(() -> {
            pane.requestFocus();
            loginButton.setOnAction(event -> handleLogin());
            registerLabel.setOnMouseClicked(event -> loadRegister());
            forgotPasswordLabel.setOnMouseClicked(event -> loadForgotPassword());

            // Menangani tekan tombol Enter di emailField
            emailField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleLogin();
                }
            });
            // Menangani tekan tombol Enter di passwordField
            passwordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleLogin();
                }
            });
        });
    }

    private void handleLogin() {
        String input = emailField.getText();
        String password = passwordField.getText();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.showToast((Stage) loginButton.getScene().getWindow(), "Semua kolom harus diisi", "info");
            return;
        }

        if (password.length() < 8) {
            Toast.showToast((Stage) loginButton.getScene().getWindow(), "Password minimal harus 8 karakter", "error");
            return;
        }

        progressIndicator.setVisible(true);

        new Thread(() -> {
            String sql = "SELECT * FROM user WHERE (email = ? OR username = ?)";
            boolean success = false;

            try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (conn != null) {
                    pstmt.setString(1, input);
                    pstmt.setString(2, input);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (checkPassword(password, storedPassword)) {
                            success = true;
                            int userId = rs.getInt("id");
                            sessionManager.setLoggedIn(true);
                            sessionManager.setCurrentUserEmail(rs.getString("email"));
                            sessionManager.setCurrentUserId(userId);
                        } else {
                            Platform.runLater(() -> Toast.showToast((Stage) loginButton.getScene().getWindow(), "Email atau password salah", "error"));
                        }
                    } else {
                        Platform.runLater(() -> Toast.showToast((Stage) loginButton.getScene().getWindow(), "Email atau password salah", "error"));
                    }
                } else {
                    Platform.runLater(() -> Toast.showToast((Stage) loginButton.getScene().getWindow(), "Gagal menghubungkan ke database", "error"));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> Toast.showToast((Stage) loginButton.getScene().getWindow(), "Terjadi kesalahan: " + e.getMessage(), "error"));
            }

            final boolean loginSuccess = success;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (loginSuccess) {
                    Toast.showToast((Stage) loginButton.getScene().getWindow(), "Login berhasil!", "success");
                    loadDashboard();
                }
            });
        }).start();
    }

    private boolean checkPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Kesalahan saat melakukan hash pada password", e);
        }
    }

    private void loadDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }

    private void loadRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
            Stage stage = (Stage) registerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }

    private void loadForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ForgotPassword.fxml"));
            Stage stage = (Stage) forgotPasswordLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }
}
