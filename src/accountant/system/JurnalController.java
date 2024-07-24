package accountant.system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;

public class JurnalController implements Initializable {

    @FXML
    private TableView<Jurnal> tabelJurnal;
    @FXML
    private ComboBox<String> filterBulan;
    @FXML
    private ComboBox<String> filterTahun;
    @FXML
    private Button tambahTransaksi;

    private double xOffset = 0;
    private double yOffset = 0;

    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Platform.runLater(() -> {
            tabelJurnal.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Inisialisasi choice box filter bulan
            filterBulan.getItems().addAll("Semua", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember");
            filterBulan.setValue("Semua");

            // Inisialisasi choice box filter tahun
            for (int year = LocalDate.now().getYear(); year >= 2000; year--) {
                filterTahun.getItems().add(String.valueOf(year));
            }
            filterTahun.setValue(String.valueOf(LocalDate.now().getYear()));

            // Handle button action to show popup
            tambahTransaksi.setOnAction(event -> handleModal());

            // Memuat data jurnal ke dalam tabel
            try {
                loadDataToTable();
            } catch (SQLException e) {
                System.out.println("Error : " + e);
            }

            // Menambahkan listener untuk ComboBox filterBulan dan filterTahun
            filterBulan.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    loadDataToTable(); // Panggil loadDataToTable() saat pilihan di ComboBox berubah
                } catch (SQLException e) {
                    System.out.println("Error : " + e);
                }
            });

            filterTahun.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    loadDataToTable(); // Panggil loadDataToTable() saat pilihan di ComboBox berubah
                } catch (SQLException e) {
                    System.out.println("Error : " + e);
                }
            });
        });
    }

    private void handleModal() {
        try {
            // Load the FXML file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TambahTransaksi.fxml"));
            Parent root = loader.load();

            // Create a new stage
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.UNDECORATED); // Set stage style to undecorated
            popupStage.initModality(Modality.APPLICATION_MODAL); // Set modality to block input events to other windows

            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                popupStage.setX(event.getScreenX() - xOffset);
                popupStage.setY(event.getScreenY() - yOffset);
            });

            // Set the scene with the loaded FXML content
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait(); // Show the stage and wait for it to be closed before returning control

            // Refresh table after adding transaction
            loadDataToTable();

        } catch (IOException | SQLException e) {
            System.out.println("Error : " + e);
        }
    }

    private void loadDataToTable() throws SQLException {
        ObservableList<Jurnal> jurnalList = FXCollections.observableArrayList();

        String sql = "SELECT j.id, j.tanggal, j.deskripsi, j.total_transaksi, j.link, dj.akun_id "
                + "FROM jurnal j "
                + "JOIN detail_jurnal dj ON j.id = dj.jurnal_id "
                + "WHERE j.user_id = ?";

        // Tambahkan filter bulan dan tahun jika dipilih
        if (!filterBulan.getValue().equals("Semua")) {
            sql += " AND SUBSTR(j.tanggal, 4, 2) = ?";
        }
        sql += " AND SUBSTR(j.tanggal, 7, 4) = ? ORDER BY tanggal ASC";

        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, sessionManager.getCurrentUserId());

            int paramIndex = 2; // Parameter indeks dimulai dari 2 karena parameter pertama adalah user_id
            if (!filterBulan.getValue().equals("Semua")) {
                int monthIndex = filterBulan.getItems().indexOf(filterBulan.getValue()); // Adjust month index to match SQL format
                String monthString = String.format("%02d", monthIndex);
                pstm.setString(paramIndex++, monthString);
            }
            pstm.setString(paramIndex, filterTahun.getValue());

            ResultSet rs = pstm.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate tanggal = LocalDate.parse(rs.getString("tanggal"), formatter);
                String deskripsi = rs.getString("deskripsi");
                int totalTransaksi = rs.getInt("total_transaksi");
                String link = rs.getString("link");
                int kodeAkun = rs.getInt("akun_id"); // Mengambil kode akun dari detail_jurnal

                Jurnal jurnal = new Jurnal(id, kodeAkun, tanggal, deskripsi, totalTransaksi, link);
                jurnalList.add(jurnal);
            }

        } catch (SQLException e) {
            System.out.println("Error : " + e);
            throw e;
        }

        // Set properties for table columns
        tabelJurnal.getColumns().clear();
        TableColumn<Jurnal, Integer> kodeAkunCol = new TableColumn<>("Kode Akun");
        kodeAkunCol.setCellValueFactory(new PropertyValueFactory<>("kodeAkun"));
        kodeAkunCol.setMinWidth(75);
        kodeAkunCol.setMaxWidth(75);
        kodeAkunCol.setSortable(false);

        TableColumn<Jurnal, LocalDate> tanggalCol = new TableColumn<>("Tanggal");
        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        tanggalCol.setMinWidth(75);
        tanggalCol.setMaxWidth(75);
        tanggalCol.setSortable(false);

        // Custom cell factory to format date
        tanggalCol.setCellFactory(column -> {
            TableCell<Jurnal, LocalDate> cell = new TableCell<Jurnal, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
            return cell;
        });

        TableColumn<Jurnal, String> deskripsiCol = new TableColumn<>("Deskripsi");
        deskripsiCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        deskripsiCol.setMinWidth(75);
        deskripsiCol.setMaxWidth(Double.MAX_VALUE);
        deskripsiCol.setSortable(false);

        TableColumn<Jurnal, Integer> totalTransaksiCol = new TableColumn<>("Total Transaksi");
        totalTransaksiCol.setCellValueFactory(new PropertyValueFactory<>("totalTransaksi"));
        totalTransaksiCol.setMinWidth(100);
        totalTransaksiCol.setMaxWidth(100);
        totalTransaksiCol.setSortable(false);

        TableColumn<Jurnal, String> linkCol = new TableColumn<>("Link Dokumen");
        linkCol.setCellValueFactory(new PropertyValueFactory<>("linkDokumen"));
        linkCol.setMinWidth(180);
        linkCol.setMaxWidth(180);
        linkCol.setSortable(false);

        // Custom cell factory to create clickable hyperlinks
        linkCol.setCellFactory(column -> {
            return new TableCell<Jurnal, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Hyperlink hyperlink = new Hyperlink(item);
                        hyperlink.setOnAction(event -> {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(item));
                            } catch (java.io.IOException | java.net.URISyntaxException e) {
                                System.err.println("Error opening link: " + e.getMessage());
                            }
                        });
                        setGraphic(hyperlink);
                    }
                }
            };
        });

        // Add columns to table view
        tabelJurnal.setItems(jurnalList);
        tabelJurnal.getColumns().addAll(kodeAkunCol, tanggalCol, deskripsiCol, totalTransaksiCol, linkCol);
    }
}
