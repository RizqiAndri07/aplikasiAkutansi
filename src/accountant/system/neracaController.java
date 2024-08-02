package accountant.system;

import java.io.File;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.print.PrinterJob;

public class neracaController {
    @FXML
    private Label at_122;
    @FXML
    private Label at_620;
    @FXML
    private Label at_600;
    @FXML
    private Label at_123;
    @FXML
    private Label at_total;
    @FXML
    private Label total_asset;
    @FXML
    private Label al_111;
    @FXML
    private Label al_212;
    @FXML
    private Label al_211;
    @FXML
    private Label al_800;
    @FXML
    private Label al_115;
    @FXML
    private Label al_113;
    @FXML
    private Label al_total;
    @FXML
    private Label kjp_621;
    @FXML
    private Label kjp_610;
    @FXML
    private Label kjp_total;
    @FXML
    private Label e_300;
    @FXML
    private Label e_401;
    @FXML
    private Label e_400;
    @FXML
    private Label e_total;
    @FXML
    private Label total_le;
    @FXML
    private Label filterTahun;
    @FXML
    private Label namaPerusahaan;
    @FXML
    private AnchorPane containerNeraca;
    @FXML
    private Button download;
    
    @FXML
    private ComboBox<Integer> tahun;

    // List of years
    private final List<Integer> tahunList = Arrays.asList(
            2022, 2023, 2024, 2025, 2026
    );

    private final SessionManager sessionmanager = SessionManager.getInstance();

    // Map to store raw values
    private final Map<Integer, Double> rawValues = new HashMap<>();

    @FXML
    private void initialize() {
        // Initialize tahun ComboBox
        tahun.setItems(FXCollections.observableArrayList(tahunList));
        tahun.getSelectionModel().selectFirst(); // Select the first year by default
        tahun.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                select();
            }
        });

        try {
            loadData();
        } catch (SQLException ex) {
            Logger.getLogger(neracaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadData() throws SQLException {
    // Query utama untuk data akun
    String akunQuery = """
                       SELECT akun_id, debit, kredit 
                       FROM buku_besar
                       WHERE akun_id IN (122, 620, 600, 123, 111, 212, 211, 800, 115, 113, 621, 610, 300, 401, 400)
                       AND user_id = ?
                       """;

    // Query untuk nama perusahaan
    String companyQuery = "SELECT nama_perusahaan FROM user WHERE id = ?";

    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    Map<Integer, Label> labelMap = new HashMap<>();
    labelMap.put(122, at_122);
    labelMap.put(620, at_620);
    labelMap.put(600, at_600);
    labelMap.put(123, at_123);
    labelMap.put(111, al_111);
    labelMap.put(212, al_212);
    labelMap.put(211, al_211);
    labelMap.put(800, al_800);
    labelMap.put(115, al_115);
    labelMap.put(113, al_113);
    labelMap.put(621, kjp_621);
    labelMap.put(610, kjp_610);
    labelMap.put(300, e_300);
    labelMap.put(401, e_401);
    labelMap.put(400, e_400);

    rawValues.clear();

    try (Connection conn = Database.connect()) {
        // Load nama perusahaan
        try (PreparedStatement companyStmt = conn.prepareStatement(companyQuery)) {
            companyStmt.setInt(1, sessionmanager.getCurrentUserId()); // Set user ID from session
            try (ResultSet rs = companyStmt.executeQuery()) {
                if (rs.next()) {
                    namaPerusahaan.setText(rs.getString("nama_perusahaan"));
                } else {
                    namaPerusahaan.setText("Nama Perusahaan Tidak Tersedia");
                }
            }
        }

        // Load data akun
        try (PreparedStatement pstmt = conn.prepareStatement(akunQuery)) {
            pstmt.setInt(1, sessionmanager.getCurrentUserId()); // Set user ID from session
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int akunId = rs.getInt("akun_id");
                    double debit = rs.getDouble("debit");
                    double kredit = rs.getDouble("kredit");
                    double nilai = debit - kredit; // Net value by subtracting credit from debit

                    // Accumulate the value for the same akun_id
                    rawValues.put(akunId, rawValues.getOrDefault(akunId, 0.0) + nilai);
                }

                for (Map.Entry<Integer, Double> entry : rawValues.entrySet()) {
                    int akunId = entry.getKey();
                    double nilai = entry.getValue();
                    String formattedSaldo = decimalFormat.format(nilai);

                    Label label = labelMap.get(akunId);
                    if (label != null) {
                        label.setText(formattedSaldo);
                    }
                }

                calculateTotals(decimalFormat);
            }
        }
    }
}

    private void calculateTotals(DecimalFormat decimalFormat) {
        double atTotal = sumRawValues(122, 620, 600, 123);
        at_total.setText(decimalFormat.format(atTotal));

        double alTotal = sumRawValues(111, 212, 211, 800, 115, 113);
        al_total.setText(decimalFormat.format(alTotal));

        total_asset.setText(decimalFormat.format(atTotal + alTotal));

        double kjpTotal = sumRawValues(621, 610);
        kjp_total.setText(decimalFormat.format(kjpTotal));

        double eTotal = sumRawValues(300, 401, 400);
        e_total.setText(decimalFormat.format(eTotal));

        total_le.setText(decimalFormat.format(eTotal + kjpTotal));
    }

    private double sumRawValues(Integer... akunIds) {
        return Arrays.stream(akunIds)
                     .mapToDouble(id -> rawValues.getOrDefault(id, 0.0))
                     .sum();
    }

    @FXML
    private void select() {
        Integer selectedTahun = tahun.getSelectionModel().getSelectedItem();
        if (selectedTahun != null) {
            filterTahun.setText(selectedTahun.toString());
            System.out.println("Selected Year: " + selectedTahun);
            // Additional logic to filter data based on the selected year
            // Implement filter logic here if needed
        }
    }

    @FXML
    private void handleDownload() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(containerNeraca.getScene().getWindow())) {
            boolean success = printerJob.printPage(containerNeraca);
            if (success) {
                printerJob.endJob();
            }
        }
    }
}
