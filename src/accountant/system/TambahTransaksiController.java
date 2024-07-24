package accountant.system;

import java.net.URL;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TambahTransaksiController implements Initializable {

    @FXML
    private Button close;
    @FXML
    private DatePicker tanggal;
    @FXML
    private ComboBox<AkunItem> subAkun;
    @FXML
    private TextField saldo;
    @FXML
    private TextArea catatan;
    @FXML
    private TextField buktiDokumen;
    @FXML
    private ComboBox<String> jenisPembayaran;
    @FXML
    private Button simpan;

    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadSubAkun3();
        } catch (SQLException ex) {
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Error loading sub accounts: " + ex.getMessage(), "error");
        }

        jenisPembayaran.getItems().addAll("Debit", "Kredit");
        jenisPembayaran.setValue("Jenis Transaksi");

        close.setOnMouseClicked(event -> handleClose());
        simpan.setOnAction(event -> handleSimpan());
    }

    private void loadSubAkun3() throws SQLException {
        ObservableList<AkunItem> subAkunList = FXCollections.observableArrayList();
        subAkunList.add(new AkunItem(0, "Pilih Sub akun"));

        String sql = "SELECT id, nama FROM akun_3";

        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sql); ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                subAkunList.add(new AkunItem(id, nama));
            }
        } catch (SQLException e) {
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Error loading sub accounts: " + e.getMessage(), "error");
        }

        subAkun.setItems(subAkunList);
        subAkun.setValue(subAkunList.get(0));
    }

    private void handleSimpan() {
        // Validasi untuk memastikan semua kolom terisi
        if (saldo.getText().isEmpty() || jenisPembayaran.getValue() == null
                || subAkun.getValue().getNama().equals("Pilih Sub akun") || tanggal.getValue() == null
                || buktiDokumen.getText().isEmpty() || catatan.getText().isEmpty()) {

            Toast.showToast((Stage) simpan.getScene().getWindow(), "Harap lengkapi semua kolom sebelum menyimpan transaksi", "error");
            return; // Hentikan proses jika ada kolom yang kosong
        }

        Toast.showConfirmationPopup((Stage) simpan.getScene().getWindow(), "Konfirmasi Simpan Transaksi", "Apakah Anda yakin ingin menyimpan transaksi ini?", () -> {
            // Yes
            saveTransaction();
            handleClose();
        }, () -> {
            // Cancel
//            Toast.showToast((Stage) simpan.getScene().getWindow(), "Transaksi dibatalkan", "info");
            return;
        });
    }

    private void saveTransaction() {
        try {
            int saldoValue = Integer.parseInt(saldo.getText());
            String jenisTransaksi = jenisPembayaran.getValue();
            AkunItem selectedAkun = subAkun.getValue();
            int kodeAkun3 = selectedAkun.getIdAkun();

            String getLastSaldoSql = "SELECT saldo FROM buku_besar WHERE akun_id = ? ORDER BY tanggal DESC, id DESC LIMIT 1";
            try (Connection conn = Database.connect()) {
                int lastSaldo = 0;

                // Ambil saldo terakhir dari buku_besar berdasarkan akun_id
                try (PreparedStatement pstm = conn.prepareStatement(getLastSaldoSql)) {
                    pstm.setInt(1, kodeAkun3);
                    ResultSet rs = pstm.executeQuery();
                    if (rs.next()) {
                        lastSaldo = rs.getInt("saldo");
                    }
                }

                if (jenisTransaksi.equals("Debit")) {
                    saveToJurnal(kodeAkun3, saldoValue, 0);
                } else if (jenisTransaksi.equals("Kredit")) {
                    // Membandingkan saldo akhir dengan saldo yang ingin dikeluarkan
                    if (saldoValue > lastSaldo) {
//                        showErrorDialog();
                        Toast.showToast((Stage) simpan.getScene().getWindow(), "Saldo tidak mencukupi", "error");
                    }
                    saveToJurnal(kodeAkun3, 0, saldoValue);
                }
            } catch (SQLException ex) {
                System.out.println("Error sql: " + ex);
            }
        } catch (NumberFormatException e) {
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Saldo harus berupa angka", "error");
        }
    }

    private void saveToJurnal(int kodeAkun3, int debit, int kredit) {
        String sql = "INSERT INTO jurnal (tanggal, deskripsi, total_transaksi, link, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, tanggal.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            pstm.setString(2, catatan.getText());
            if (debit == 0) {
                pstm.setInt(3, kredit);
            } else {
                pstm.setInt(3, debit);
            }
            pstm.setString(4, buktiDokumen.getText());
            pstm.setInt(5, sessionManager.getCurrentUserId());

            pstm.executeUpdate();

            ResultSet generatedKeys = pstm.getGeneratedKeys();
            int jurnalId = -1;
            if (generatedKeys.next()) {
                jurnalId = generatedKeys.getInt(1);
            }

            // Simpan detail jurnal
            saveDetailJurnal(jurnalId, kodeAkun3, debit, kredit);

            Toast.showToast((Stage) simpan.getScene().getWindow(), "Transaksi berhasil disimpan", "success");

        } catch (SQLException e) {
            System.out.println(e);
//            showErrorDialog();
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Error saving transaction: " + e.getMessage(), "error");
        }
    }

    private void saveDetailJurnal(int jurnalId, int kodeAkun3, int debit, int kredit) {
        String sql = "INSERT INTO detail_jurnal (jurnal_id, akun_id, debit, kredit) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, jurnalId);
            pstm.setInt(2, kodeAkun3);
            pstm.setInt(3, debit);
            pstm.setInt(4, kredit);

            pstm.executeUpdate();

            // Update buku besar
            updateBukuBesar(kodeAkun3, debit, kredit);

        } catch (SQLException e) {
//            showErrorDialog();
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Error saving detail jurnal: " + e.getMessage(), "error");
        }
    }

    private void updateBukuBesar(int kodeAkun3, int debit, int kredit) {
        String getLastSaldoSql = "SELECT saldo FROM buku_besar WHERE akun_id = ? ORDER BY tanggal DESC, id DESC LIMIT 1";
        String insertSql = "INSERT INTO buku_besar (tanggal, akun_id, deskripsi, debit, kredit, saldo, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect()) {
            int lastSaldo = 0;

            // Ambil saldo terakhir dari buku_besar berdasarkan akun_id
            try (PreparedStatement pstm = conn.prepareStatement(getLastSaldoSql)) {
                pstm.setInt(1, kodeAkun3);
                ResultSet rs = pstm.executeQuery();
                if (rs.next()) {
                    lastSaldo = rs.getInt("saldo");
                }
            }

            // Hitung saldo baru
            int newSaldo;
            if (debit == 0) {
                newSaldo = lastSaldo - kredit;
            } else {
                newSaldo = lastSaldo + debit;
            }

            // Masukkan catatan baru ke buku_besar
            try (PreparedStatement pstm = conn.prepareStatement(insertSql)) {
                pstm.setString(1, tanggal.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                pstm.setInt(2, kodeAkun3);
                pstm.setString(3, catatan.getText());
                pstm.setInt(4, debit);
                pstm.setInt(5, kredit);
                pstm.setInt(6, newSaldo);
                pstm.setInt(7, sessionManager.getCurrentUserId());

                pstm.executeUpdate();
            }
        } catch (SQLException e) {
//            showErrorDialog();
            Toast.showToast((Stage) simpan.getScene().getWindow(), "Error updating buku besar: " + e.getMessage(), "error");
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleClose() {
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
    }

    public class AkunItem {

        private final int id;
        private final String nama;

        public AkunItem(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        public int getIdAkun() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        @Override
        public String toString() {
            return nama;
        }
    }
}
