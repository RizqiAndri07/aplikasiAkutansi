package accountant.system;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.stage.Stage;

public class ResetPasswordController implements Initializable {

    @FXML
    private TextField resetTokenField;
    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField confirmNewPassword;
    @FXML
    private Button resetButton;
    @FXML
    private Label loginLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private String email;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resetButton.setOnAction(event -> handleResetPassword());
        loginLabel.setOnMouseClicked(event -> loadLogin());
    }

    public void setEmail(String email) {
        this.email = email; // Setter method to set the email
        System.out.println("EMAIL SENT: " + email);
    }

    private void handleResetPassword() {
        progressIndicator.setVisible(true);
        new Thread(() -> {
            String token = resetTokenField.getText();
            String newPass = newPassword.getText();
            String confirmPass = confirmNewPassword.getText();

            if (!newPass.equals(confirmPass)) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    Toast.showToast((Stage) resetButton.getScene().getWindow(), "Passwords do not match.", "error");
                });
                return;
            }

            String sqlCheckToken = "SELECT reset_token FROM user WHERE email = ?";
            String sqlUpdatePassword = "UPDATE user SET password = ?, reset_token = NULL WHERE email = ? AND reset_token = ?";
            boolean success = false;

            try (Connection conn = Database.connect(); PreparedStatement pstmtCheckToken = conn.prepareStatement(sqlCheckToken); PreparedStatement pstmtUpdatePassword = conn.prepareStatement(sqlUpdatePassword)) {
                System.out.println("EMAIL TO RESET PASS: " + email);

                pstmtCheckToken.setString(1, email);
                ResultSet rs = pstmtCheckToken.executeQuery();

                if (rs.next()) {
                    String dbToken = rs.getString("reset_token");
                    System.out.println("Database Token: " + dbToken);
                    System.out.println("User Token: " + token);
                    if (token.equals(dbToken)) {
                        pstmtUpdatePassword.setString(1, newPass);
                        pstmtUpdatePassword.setString(2, email);
                        pstmtUpdatePassword.setString(3, token);
                        int rowsUpdated = pstmtUpdatePassword.executeUpdate();
                        System.out.println("RU: " + rowsUpdated);
                        if (rowsUpdated > 0) {
                            success = true;
                        } else {
                            Platform.runLater(() -> Toast.showToast((Stage) resetButton.getScene().getWindow(), "Failed to update password.", "error"));
                        }
                    } else {
                        Platform.runLater(() -> Toast.showToast((Stage) resetButton.getScene().getWindow(), "Invalid reset token.", "error"));
                    }
                } else {
                    Platform.runLater(() -> Toast.showToast((Stage) resetButton.getScene().getWindow(), "Email not found.", "error"));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> Toast.showToast((Stage) resetButton.getScene().getWindow(), "An error occurred1: " + e.getMessage(), "error"));
            }

            final boolean resetSuccess = success;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (resetSuccess) {
                    Toast.showToast((Stage) resetButton.getScene().getWindow(), "Password reset successfully!", "success");
                    loadLogin();
                }
            });
        }).start();
    }

    private void loadLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) loginLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            Toast.showToast((Stage) loginLabel.getScene().getWindow(), "An error occurred2: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
}
