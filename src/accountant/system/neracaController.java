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
import javafx.scene.Node;

public class neracaController {
    @FXML
    private Label at_121, at_124, at_total, total_asset;
    @FXML
    private Label al_111, al_122, al_114, al_103, al_115, al_113, al_108, al_total;
    @FXML
    private Label utpn_211, utpn_total;
    @FXML
    private Label m_300, m_312, m_total;
    @FXML
    private Label utpj_212, utpj_total, total_le;
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
        // Listener untuk mendeteksi perubahan pada scene dan window
        containerNeraca.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        // Listener untuk mendeteksi perubahan ukuran window
                        newWindow.widthProperty().addListener((observableWidth, oldWidth, newWidth) -> adjustFontSize());
                        newWindow.heightProperty().addListener((observableHeight, oldHeight, newHeight) -> adjustFontSize());
                    }
                });
            }
        });
        
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
        labelMap.put(121, at_121);
        labelMap.put(124, at_124);
        labelMap.put(111, al_111);
        labelMap.put(122, al_122);
        labelMap.put(114, al_114);
        labelMap.put(103, al_103);
        labelMap.put(115, al_115);
        labelMap.put(113, al_113);
        labelMap.put(108, al_108);
        labelMap.put(211, utpn_211);
        labelMap.put(212, utpj_212);
        labelMap.put(300, m_300);
        labelMap.put(312, m_312);
    }

    private void loadData(int selectedYear) throws SQLException {
        resetLabelsToZero(); // Reset labels sebelum memuat data baru

        String akunQuery = """
            SELECT akun_id, debit, kredit 
            FROM buku_besar
            WHERE akun_id IN (121, 124, 111, 122, 114, 103, 115,113, 108, 211, 212, 300, 312)
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

    private void adjustFontSize() {
        double width = containerNeraca.getScene().getWindow().getWidth();
        double height = containerNeraca.getScene().getWindow().getHeight();

        // Tentukan ukuran font yang diinginkan berdasarkan ukuran window
        double fontSize = (width > 800 && height > 600) ? 14 + 2 : 14; // +2 saat maximize

        // Iterasi melalui semua anak dari containerNeraca
        for (Node node : containerNeraca.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-font-size: " + fontSize + "px;");
            }
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
        utpn_total.setText(decimalFormat.format(0));
        utpj_total.setText(decimalFormat.format(0));
        total_le.setText(decimalFormat.format(0));
    }

    private void calculateTotals(DecimalFormat decimalFormat) {
        double atTotal = sumRawValues(121, 124);
        double alTotal = sumRawValues(111, 113, 122, 114, 115, 103, 108);
        double utpnTotal = sumRawValues(211);
        double utpjTotal = sumRawValues(212);
        double mTotal = sumRawValues(300, 312);

        at_total.setText(decimalFormat.format(atTotal));
        al_total.setText(decimalFormat.format(alTotal));
        total_asset.setText(decimalFormat.format(atTotal + alTotal));
        utpn_total.setText(decimalFormat.format(utpnTotal));
        utpj_total.setText(decimalFormat.format(utpjTotal));
        m_total.setText(decimalFormat.format(mTotal));
        total_le.setText(decimalFormat.format(utpjTotal + utpnTotal + mTotal));
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
                resetLabelsToZero(); // Reset labels jika terjadi error
            }
        }
    }

    @FXML
    private void handleDownload() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(containerNeraca.getScene().getWindow())) {
            PageLayout pageLayout = printerJob.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);

            // Ambil ukuran asli dan saat ini dari containerNeraca
            double originalWidth = containerNeraca.getWidth();
            double originalHeight = containerNeraca.getHeight();

            // Hitung skala berdasarkan ukuran asli dan ukuran layout page
            double scaleX = pageLayout.getPrintableWidth() / originalWidth;
            double scaleY = pageLayout.getPrintableHeight() / originalHeight;
            double scaleFactor = Math.min(scaleX, scaleY);

            // Terapkan skala
            containerNeraca.getTransforms().add(new Scale(scaleFactor, scaleFactor));

            // Geser posisi ke kiri (negatif) sebanyak yang Anda butuhkan
            double translateX = -130; // Ganti angka ini dengan nilai yang Anda inginkan
            containerNeraca.getTransforms().add(new Translate(translateX, 0));

            boolean success = printerJob.printPage(pageLayout, containerNeraca);
            if (success) {
                printerJob.endJob();
            }

            // Bersihkan transformasi setelah pencetakan selesai
            containerNeraca.getTransforms().clear();
        } else {
            System.out.println("Failed to create Printer Job or print dialog was cancelled");
        }
    }
}
