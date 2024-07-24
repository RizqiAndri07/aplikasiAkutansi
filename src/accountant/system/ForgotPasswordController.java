package accountant.system;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ForgotPasswordController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private Button sendButton;
    @FXML
    private Label registerLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    ResetPasswordController resetPasswordController = new ResetPasswordController();
    @FXML
    private AnchorPane isConnected;
    @FXML
    private Button cancel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sendButton.setOnAction(event -> handleSendResetToken());
        registerLabel.setOnMouseClicked(event -> loadRegister());
        cancel.setOnMouseClicked(event -> loadLogin());
    }

    private void handleSendResetToken() {
        progressIndicator.setVisible(true);
        new Thread(() -> {
            String email = emailField.getText();
            String token = generateResetToken();
            boolean emailExists = false;

            String checkEmailSql = "SELECT * FROM user WHERE email = ?";
            String updateTokenSql = "UPDATE user SET reset_token = ? WHERE email = ?";

            try (Connection conn = Database.connect(); PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql); PreparedStatement updateStmt = conn.prepareStatement(updateTokenSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    emailExists = true;
                }

                if (emailExists) {
                    updateStmt.setString(1, token);
                    updateStmt.setString(2, email);
                    updateStmt.executeUpdate();
                    resetPasswordController.setEmail(email);
                    sendResetEmail(email, token);
                    Platform.runLater(() -> {
                        Toast.showToast((Stage) sendButton.getScene().getWindow(), "Reset Token sent, please check your email!", "success");
                        loadResetPassword(email);
                    });
                } else {
                    Platform.runLater(() -> {
                        Toast.showToast((Stage) sendButton.getScene().getWindow(), "Email not found", "error");
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    Toast.showToast((Stage) sendButton.getScene().getWindow(), "An error occurred: " + e.getMessage(), "error");
                });
            } finally {
                Platform.runLater(() -> progressIndicator.setVisible(false));
            }
        }).start();
    }

    private String generateResetToken() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6-digit token
    }

    private void sendResetEmail(String email, String token) {
        String subject = "Password Reset Request";
        String body = "Your password reset token is: " + token;
        EmailUtil.sendEmail(email, subject, body); // Assuming EmailUtil is a utility class for sending emails
    }

    private void loadRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
            Stage stage = (Stage) registerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) registerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResetPassword(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ResetPassword.fxml"));
            Parent root = loader.load();
            ResetPasswordController resetPasswordController = loader.getController(); // Get controller instance

            resetPasswordController.setEmail(email); // Set email in ResetPasswordController

            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
