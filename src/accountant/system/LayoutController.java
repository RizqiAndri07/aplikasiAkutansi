package accountant.system;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class LayoutController implements Initializable {

    @FXML
    private JFXButton jurnal;
    @FXML
    private JFXButton bukuBesar;
    @FXML
    private JFXButton neraca;
    @FXML
    private JFXButton lapLabaRugi;
    @FXML
    private JFXButton logOut;

    private SessionManager sessionManager;
    private Stage mainStage;
    @FXML
    private AnchorPane anchorLayout;

    private JFXButton activeButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sessionManager = SessionManager.getInstance();

        // Gunakan Platform.runLater untuk menunda eksekusi inisialisasi sampai Scene benar-benar siap
        Platform.runLater(() -> {
            // Simpan stage utama
            mainStage = (Stage) anchorLayout.getScene().getWindow();
            anchorLayout.requestFocus();

            // Set event handler untuk setiap tombol navigasi
            jurnal.setOnMouseClicked(event -> handleNavigation("Jurnal.fxml", jurnal));
            bukuBesar.setOnMouseClicked(event -> handleNavigation("BukuBesar.fxml", bukuBesar));
            neraca.setOnMouseClicked(event -> handleNavigation("neraca.fxml", neraca));
            lapLabaRugi.setOnMouseClicked(event -> switchScene("LapLabaRugi.fxml"));
            logOut.setOnMouseClicked(event -> handleLogout());
        });
    }

    private void handleNavigation(String resource, JFXButton button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Simpan status ukuran dan maximize sebelum beralih
            boolean isMaximized = mainStage.isMaximized();
            double width = mainStage.getWidth();
            double height = mainStage.getHeight();

            // Set ukuran minimum
            mainStage.setMinWidth(850);
            mainStage.setMinHeight(450);

            // Pusatkan mainStage di layar
//            mainStage.centerOnScreen();
            // Set scene baru
            mainStage.setResizable(true);
            mainStage.setScene(scene);

            // Atur ulang ukuran dan maximize jika sebelumnya maximized
            if (isMaximized) {
                mainStage.setMaximized(true);
            } else {
                mainStage.setWidth(width);
                mainStage.setHeight(height);
            }

            // Update tombol aktif
            setActiveButton(button);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    private void switchScene(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Nonaktifkan kemampuan untuk maximize
//            mainStage.setResizable(false);
            mainStage.setMaximized(false);
            
            // Atur scene baru
            mainStage.setScene(scene);

            // Perbarui ukuran mainStage sesuai dengan ukuran root dari resource yang dimuat
            mainStage.sizeToScene();

            mainStage.setMinHeight(mainStage.getHeight());
            mainStage.setMinWidth(mainStage.getWidth());

            // Pusatkan mainStage di layar
            mainStage.centerOnScreen();

            // Tampilkan mainStage jika belum ditampilkan
            if (!mainStage.isShowing()) {
                mainStage.show();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    private void switchToLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Atur scene baru tanpa memasukkan login ke dalam main stage
            Stage loginStage = new Stage();
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();

            // Tutup main stage (jika diperlukan)
            mainStage.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    private void setActiveButton(JFXButton activeButton) {
        // Menghapus gaya dari semua tombol terlebih dahulu
        jurnal.getStyleClass().remove("selected-button");
        bukuBesar.getStyleClass().remove("selected-button");
        neraca.getStyleClass().remove("selected-button");
        lapLabaRugi.getStyleClass().remove("selected-button");

        // Menambahkan gaya pada tombol yang dipilih
        activeButton.getStyleClass().add("selected-button");
    }

    private void handleLogout() {
        try {
            sessionManager.logout();
            switchToLoginScene();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

}
