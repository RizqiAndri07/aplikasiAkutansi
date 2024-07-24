package accountant.system;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class BukuBesarController implements Initializable {

    @FXML
    private ComboBox<AkunItem> filterBukuBesar;
    @FXML
    private TableView<BukuBesar> tabelBukuBesar;

    private final SessionManager sessionmanager = SessionManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadAkun3();
        } catch (SQLException ex) {
            Logger.getLogger(BukuBesarController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            loadDataToTable();
        } catch (SQLException ex) {
            Logger.getLogger(BukuBesarController.class.getName()).log(Level.SEVERE, null, ex);
        }

        filterBukuBesar.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadDataToTable();
            } catch (SQLException ex) {
                Logger.getLogger(BukuBesarController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void loadAkun3() throws SQLException {
        ObservableList<BukuBesarController.AkunItem> akunList = FXCollections.observableArrayList();
        akunList.add(new AkunItem(0, "Semua"));

        String sql = "SELECT id, nama FROM akun_3";

        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                akunList.add(new BukuBesarController.AkunItem(id, nama));
            }
        } catch (SQLException e) {
            System.out.println("Error loading accounts: " + e.getMessage());
        }

        filterBukuBesar.setItems(akunList);
        filterBukuBesar.setValue(akunList.get(0));
    }

    private void loadDataToTable() throws SQLException {
        ObservableList<BukuBesar> bukuBesarList = FXCollections.observableArrayList();
        AkunItem selectedAkun = filterBukuBesar.getValue();

        String sql = "SELECT bb.id AS buku_besar_id, bb.tanggal, bb.deskripsi,"
                + " bb.kredit, bb.debit, bb.saldo,"
                + " a3.nama AS nama_akun_3, a3.id AS kode_akun"
                + " FROM buku_besar bb"
                + " JOIN akun_3 a3 ON bb.akun_id = a3.id"
                + " JOIN user u ON u.id = bb.user_id"
                + " WHERE u.id = ?";

        if (!selectedAkun.getNama().equals("Semua")) {
            sql += " AND a3.id = ?";
        }

        sql += " ORDER BY kode_akun ASC";

        try (Connection conn = Database.connect(); PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, sessionmanager.getCurrentUserId());

            if (!selectedAkun.getNama().equals("Semua")) {
                pstm.setInt(2, selectedAkun.getIdAkun1());
            }

            ResultSet rs = pstm.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            while (rs.next()) {
                LocalDate tanggal = LocalDate.parse(rs.getString("tanggal"), formatter);
                String deskripsi = rs.getString("deskripsi");
                int debit = rs.getInt("debit");
                int kredit = rs.getInt("kredit");
                int saldo = rs.getInt("saldo");

                BukuBesar bukuBesar = new BukuBesar(tanggal, deskripsi, debit, kredit, saldo);
                bukuBesarList.add(bukuBesar);
            }
        } catch (SQLException e) {
            System.out.println("Error : " + e);
            throw e;
        }

        tabelBukuBesar.getColumns().clear();
        TableColumn<BukuBesar, LocalDate> tanggalCol = new TableColumn<>("Tanggal");
        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        tanggalCol.setCellFactory(column -> {
            TableCell<BukuBesar, LocalDate> cell = new TableCell<BukuBesar, LocalDate>() {
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
        tanggalCol.setMinWidth(75);
        tanggalCol.setMaxWidth(75);
        tanggalCol.setSortable(false);

        TableColumn<BukuBesar, String> deskripsiCol = new TableColumn<>("Deskripsi");
        deskripsiCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        deskripsiCol.setMinWidth(75);
        deskripsiCol.setMaxWidth(Double.MAX_VALUE);
        deskripsiCol.setSortable(false);

        TableColumn<BukuBesar, Integer> debitCol = new TableColumn<>("Debit");
        debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
        debitCol.setMinWidth(100);
        debitCol.setMaxWidth(100);
        debitCol.setSortable(false);

        TableColumn<BukuBesar, Integer> kreditCol = new TableColumn<>("Kredit");
        kreditCol.setCellValueFactory(new PropertyValueFactory<>("kredit"));
        kreditCol.setMinWidth(100);
        kreditCol.setMaxWidth(100);
        kreditCol.setSortable(false);

        TableColumn<BukuBesar, Integer> saldoCol = new TableColumn<>("Saldo");
        saldoCol.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        saldoCol.setMinWidth(100);
        saldoCol.setMaxWidth(100);
        saldoCol.setSortable(false);

        tabelBukuBesar.setItems(bukuBesarList);
        tabelBukuBesar.getColumns().addAll(tanggalCol, deskripsiCol, debitCol, kreditCol, saldoCol);
    }

    public class AkunItem {

        private final int id_akun1;
        private final String nama;

        public AkunItem(int id_akun1, String nama) {
            this.id_akun1 = id_akun1;
            this.nama = nama;
        }

        public int getIdAkun1() {
            return id_akun1;
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
