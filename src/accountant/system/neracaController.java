package accountant.system;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.print.*;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class neracaController {
    @FXML
    private Label at_122, at_620, at_600, at_123, at_total, total_asset;
    @FXML
    private Label al_111, al_212, al_211, al_800, al_115, al_113, al_total;
    @FXML
    private Label kjp_621, kjp_610, kjp_total;
    @FXML
    private Label e_300, e_401, e_400, e_total, total_le;
    @FXML
    private Label filterTahun, namaPerusahaan;
    @FXML
    private AnchorPane containerNeraca;
    @FXML
    private ComboBox<Integer> tahun;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final Map<Integer, Double> rawValues = new HashMap<>();
    private final Map<Integer, Label> labelMap = new HashMap<>();

    @FXML
private void initialize() {
    initializeTahunComboBox();
    initializeLabelMap();
    int currentYear = LocalDate.now().getYear();
    try {
        loadData(currentYear);
    } catch (SQLException ex) {
        Logger.getLogger(neracaController.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    private void initializeTahunComboBox() {
        int currentYear = LocalDate.now().getYear();
        List<Integer> tahunList = IntStream.rangeClosed(2015, currentYear)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        tahun.setItems(FXCollections.observableArrayList(tahunList));
        tahun.getSelectionModel().select(Integer.valueOf(currentYear));
        tahun.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                select();
            }
        });
    }

    private void initializeLabelMap() {
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
    }

   private void loadData(int selectedYear) throws SQLException {
    resetLabelsToZero(); // Reset labels to zero before loading new data

    String akunQuery = """
            SELECT akun_id, debit, kredit 
            FROM buku_besar
            WHERE akun_id IN (122, 620, 600, 123, 111, 212, 211, 800, 115, 113, 621, 610, 300, 401, 400)
            AND user_id = ? AND SUBSTRING(tanggal, 7, 4) = ?
            """;
    String companyQuery = "SELECT nama_perusahaan FROM user WHERE id = ?";
    DecimalFormat decimalFormat = new DecimalFormat("#,###");

    try (Connection conn = Database.connect();
         PreparedStatement companyStmt = conn.prepareStatement(companyQuery);
         PreparedStatement akunStmt = conn.prepareStatement(akunQuery)) {

        loadCompanyName(companyStmt);
        loadAccountData(akunStmt, decimalFormat, selectedYear);
        calculateTotals(decimalFormat);
    }
}

    private void loadCompanyName(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, sessionManager.getCurrentUserId());
        try (ResultSet rs = stmt.executeQuery()) {
            namaPerusahaan.setText(rs.next() ? rs.getString("nama_perusahaan") : "Nama Perusahaan Tidak Tersedia");
        }
    }

    private void loadAccountData(PreparedStatement stmt, DecimalFormat decimalFormat, int selectedYear) throws SQLException {
    stmt.setInt(1, sessionManager.getCurrentUserId());
    stmt.setString(2, String.valueOf(selectedYear));
    try (ResultSet rs = stmt.executeQuery()) {
        rawValues.clear();
        boolean dataFound = false;
        while (rs.next()) {
            dataFound = true;
            int akunId = rs.getInt("akun_id");
            double nilai = rs.getDouble("debit") - rs.getDouble("kredit");
            rawValues.merge(akunId, nilai, Double::sum);
        }
        if (dataFound) {
            rawValues.forEach((akunId, nilai) -> {
                Label label = labelMap.get(akunId);
                if (label != null) {
                    label.setText(decimalFormat.format(nilai));
                }
            });
        } else {
            System.out.println("No data found for year: " + selectedYear);
        }
    }
}

    private void resetLabelsToZero() {
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    labelMap.values().forEach(label -> label.setText(decimalFormat.format(0)));
    at_total.setText(decimalFormat.format(0));
    al_total.setText(decimalFormat.format(0));
    total_asset.setText(decimalFormat.format(0));
    kjp_total.setText(decimalFormat.format(0));
    e_total.setText(decimalFormat.format(0));
    total_le.setText(decimalFormat.format(0));
}
    
    private void calculateTotals(DecimalFormat decimalFormat) {
        double atTotal = sumRawValues(122, 620, 600, 123);
        double alTotal = sumRawValues(111, 212, 211, 800, 115, 113);
        double kjpTotal = sumRawValues(621, 610);
        double eTotal = sumRawValues(300, 401, 400);

        at_total.setText(decimalFormat.format(atTotal));
        al_total.setText(decimalFormat.format(alTotal));
        total_asset.setText(decimalFormat.format(atTotal + alTotal));
        kjp_total.setText(decimalFormat.format(kjpTotal));
        e_total.setText(decimalFormat.format(eTotal));
        total_le.setText(decimalFormat.format(eTotal + kjpTotal));
    }

    private double sumRawValues(Integer... akunIds) {
        return Arrays.stream(akunIds).mapToDouble(id -> rawValues.getOrDefault(id, 0.0)).sum();
    }

  @FXML
private void select() {
    Integer selectedYear = tahun.getValue();
    if (selectedYear != null) {
        filterTahun.setText(selectedYear.toString());
        System.out.println("Selected Year: " + selectedYear);
        try {
            loadData(selectedYear);
        } catch (SQLException ex) {
            Logger.getLogger(neracaController.class.getName()).log(Level.SEVERE, null, ex);
            resetLabelsToZero(); // Reset labels if an error occurs
        }
    }
}

    @FXML
    private void handleDownload() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(containerNeraca.getScene().getWindow())) {
            PageLayout pageLayout = printerJob.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);
            double scaleFactor = 1.3;
            containerNeraca.getTransforms().addAll(
                new Scale(scaleFactor, scaleFactor),
                new Translate(-140, 0)
            );

            boolean success = printerJob.printPage(pageLayout, containerNeraca);
            if (success) {
                printerJob.endJob();
            }

            containerNeraca.getTransforms().clear();
        } else {
            System.out.println("Failed to create Printer Job or print dialog was cancelled");
        }
    }
}