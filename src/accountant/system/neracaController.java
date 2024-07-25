package accountant.system;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.Line;
import javafx.scene.text.FontWeight;

public class neracaController {
    @FXML
    private ComboBox<String> bulan;
    @FXML
    private ComboBox<Integer> tahun;
    @FXML
    private GridPane containerAsetTetap;
    @FXML
    private GridPane containerAsetLancar;

    private final List<String> bulanList = Arrays.asList(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    );

    private final List<Integer> tahunList = Arrays.asList(
            2022, 2023, 2024, 2025, 2026
    );

    @FXML
    public void initialize() {
        bulan.setItems(FXCollections.observableArrayList(bulanList));
        bulan.getSelectionModel().selectFirst();

        tahun.setItems(FXCollections.observableArrayList(tahunList));
        tahun.getSelectionModel().selectFirst();

        loadGridData(containerAsetTetap, 2);
        loadGridData(containerAsetLancar, 1);
    }

    private void loadGridData(GridPane container, int akunId) {
        String query = """
                       SELECT buku_besar.deskripsi, buku_besar.saldo
                       FROM buku_besar 
                       JOIN akun_3 ON buku_besar.akun_id = akun_3.id 
                       WHERE akun_3.id_akun2 = ?;""";

        container.setVgap(10);
        double totalSaldo = 0.0;
        DecimalFormat decimalFormat = new DecimalFormat("#,###,###");

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, akunId);
            ResultSet rs = pstmt.executeQuery();

            int rowIndex = 1;
            boolean dataFound = false;

            while (rs.next()) {
                dataFound = true;
                String deskripsi = rs.getString("deskripsi");
                double saldo = rs.getDouble("saldo");
                totalSaldo += saldo;

                String formattedSaldo = decimalFormat.format(saldo);

                Label deskripsiLabel = new Label(deskripsi);
                deskripsiLabel.setFont(new Font(10));
                GridPane.setMargin(deskripsiLabel, new Insets(10, 0, 10, 10));

                Label saldoLabel = new Label(formattedSaldo);
                saldoLabel.setFont(new Font(10));
                GridPane.setMargin(saldoLabel, new Insets(10, 0, 10, 10));

                container.add(deskripsiLabel, 0, rowIndex);
                container.add(saldoLabel, 1, rowIndex);

                addRowConstraints(container);

                rowIndex++;
            }

            if (dataFound) {
                // Add separator line row
                int lineRowIndex = rowIndex;
                addSeparatorLine(container, lineRowIndex);

                // Add total labels row
                int totalRowIndex = lineRowIndex + 1;
                addTotalLabels(container, totalSaldo, decimalFormat, totalRowIndex);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRowConstraints(GridPane container) {
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(10);
        rowConstraints.setPrefHeight(30);
        rowConstraints.setVgrow(Priority.SOMETIMES);
        container.getRowConstraints().add(rowConstraints);
    }

    private void addSeparatorLine(GridPane container, int rowIndex) {
        Line line = new Line();
        line.setStartX(0.0);
        line.setEndX(100.0);
        container.add(line, 1, rowIndex);
        addRowConstraints(container);
    }

    private void addTotalLabels(GridPane container, double totalSaldo, DecimalFormat decimalFormat, int rowIndex) {
        Label totalLabel = new Label("TOTAL AKTIVA TETAP");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        GridPane.setMargin(totalLabel, new Insets(10, 0, 10, 10));
        container.add(totalLabel, 0, rowIndex);

        String formattedTotalSaldo = decimalFormat.format(totalSaldo);
        Label totalSaldoLabel = new Label(formattedTotalSaldo);
        totalSaldoLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        GridPane.setMargin(totalSaldoLabel, new Insets(10, 0, 10, 10));
        container.add(totalSaldoLabel, 2, rowIndex);

        addRowConstraints(container);
    }
}
