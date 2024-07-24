package accountant.system;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegisterController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginLabel;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private AnchorPane isConnected;
    @FXML
    private TextField namaPerusahaanField;
    @FXML
    private TextField alamatPerusahaanField;
    @FXML
    private TextField noHpField;
    @FXML
    private TextField namaPemilikField;
    @FXML
    private TextField noNPWPField;
    @FXML
    private TextField usernameField;
    @FXML
    private Button uploadLogoButton;

    private String logoFileName;
    private File temporaryFile;

    @FXML
    private TextField logoNameField;
    @FXML
    private ComboBox<String> jenisUsahaComboBox;
    @FXML
    private StackPane stackPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Platform.runLater(() -> {
            stackPane.requestFocus();

            registerButton.setOnAction(event -> handleRegister());
            loginLabel.setOnMouseClicked(event -> loadLogin());
            uploadLogoButton.setOnAction(event -> handleUploadLogo());

            // Inisialisasi ChoiceBox
            ObservableList<String> jenisUsahaComboBoxOptions = FXCollections.observableArrayList("Jasa", "Dagang", "Manufaktur");
            jenisUsahaComboBox.setItems(jenisUsahaComboBoxOptions);
            jenisUsahaComboBox.setValue("Jenis Usaha");
        });
    }

    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String namaPerusahaan = namaPerusahaanField.getText().trim();
        String alamatPerusahaan = alamatPerusahaanField.getText().trim();
        String noHp = noHpField.getText().trim();
        String jenisUsaha = jenisUsahaComboBox.getValue();
        String namaPemilik = namaPemilikField.getText().trim();
        String noNPWP = noNPWPField.getText().trim();
        String username = usernameField.getText().trim();

        // Cek apakah semua field telah diisi
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || namaPerusahaan.isEmpty() || alamatPerusahaan.isEmpty() || noHp.isEmpty()
                || jenisUsaha.equals("Jenis Usaha") || namaPemilik.isEmpty() || noNPWP.isEmpty() || username.isEmpty()
                || logoFileName == null) {
            Toast.showToast((Stage) registerButton.getScene().getWindow(), "Semua field harus diisi", "info");
            return;
        }

        if (password.length() < 8) {
            Toast.showToast((Stage) registerButton.getScene().getWindow(), "Password harus terdiri dari minimal 8 karakter", "error");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.showToast((Stage) registerButton.getScene().getWindow(), "Password tidak cocok", "error");
            return;
        }
        
        Toast.showConfirmationPopup((Stage) registerButton.getScene().getWindow(), "Konfirmasi registrasi", "Apakah Anda yakin dengan isian yang Anda berikan?", () -> {
            progressIndicator.setVisible(true);

            new Thread(() -> {
                String sqlRegistrasi = "INSERT INTO user (nama_perusahaan, alamat_perusahaan, email, no_hp, jenis_usaha, nama_pemilik, no_npwp, logo_perusahaan, username, password, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, date('now'));";

                boolean success = false;

                try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sqlRegistrasi)) {
                    // Hash password
                    String hashedPassword = hashPassword(password);

                    // Memasukkan data registrasi ke dalam tabel
                    pstm.setString(1, namaPerusahaan);
                    pstm.setString(2, alamatPerusahaan);
                    pstm.setString(3, email);
                    pstm.setString(4, noHp);
                    pstm.setString(5, jenisUsaha); // Sesuaikan dengan jenis usaha yang diinputkan atau tambahkan pilihan jenis usaha
                    pstm.setString(6, namaPemilik);
                    pstm.setString(7, noNPWP);
                    String logoFileNameNew = generateUniqueFileName(logoFileName);
                    File destinationFile = new File("src/accountant/img/" + logoFileNameNew); // Ganti dengan path folder tujuan Anda
                    // Salin file yang dipilih ke tujuan
                    Files.copy(temporaryFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    pstm.setString(8, logoFileNameNew); // Nama file logo perusahaan
                    pstm.setString(9, username);
                    pstm.setString(10, hashedPassword);
                    pstm.executeUpdate();
                    success = true;

                } catch (SQLException e) {
                    Platform.runLater(() -> Toast.showToast((Stage) registerButton.getScene().getWindow(), "Terjadi kesalahan: " + e.getMessage(), "error"));
                } catch (IOException ex) {
                    Logger.getLogger(RegisterController.class.getName()).log(Level.SEVERE, null, ex);
                }

                final boolean registrasiSukses = success;
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    if (registrasiSukses) {
                        System.out.println("Pengguna berhasil didaftarkan!");
                        Toast.showToast((Stage) registerButton.getScene().getWindow(), "Pengguna berhasil didaftarkan!", "success");
                        loadLogin();
                    } else {
                        Toast.showToast((Stage) registerButton.getScene().getWindow(), "Pendaftaran gagal", "error");
                    }
                });
            }).start();
        }, ()-> {
            return;
        });
    }

    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Gambar", "*.png", "*.jpg", "*.jpeg")
        );

        // Tampilkan dialog buka file
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Hasilkan nama file unik untuk menghindari penimpaan file yang ada
            logoFileName = selectedFile.getName();
            // Perbarui logoNameField
            logoNameField.setText(logoFileName);
            // Set temporaryFile ke selectedFile untuk referensi di masa mendatang
            temporaryFile = selectedFile;
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        // Logika menghasilkan nama file unik (misalnya, tambahkan timestamp atau UUID)
        String uniqueID = UUID.randomUUID().toString();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return uniqueID + fileExtension;
    }

    private void loadLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) loginLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Kesalahan: " + e);
        }
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
}
