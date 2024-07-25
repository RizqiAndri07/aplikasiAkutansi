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
import javafx.geometry.Insets;
import javafx.scene.shape.Line;
import javafx.scene.text.FontWeight;

public class neracaController {

    @FXML
    private GridPane containerAsetTetap;

    @FXML
    public void initialize() {
        // Panggil metode untuk mengambil data dari database saat inisialisasi
        fetchDataFromDatabase();
    }

    private void fetchDataFromDatabase() {
    String query = """
                   SELECT buku_besar.deskripsi, buku_besar.saldo
                   FROM buku_besar 
                   JOIN akun_3 ON buku_besar.akun_id = akun_3.id 
                   WHERE akun_3.id_akun2 = 1;""";
    containerAsetTetap.setVgap(10);
    try (Connection conn = Database.connect();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        int rowIndex = 1;
        boolean dataFound = false;

        while (rs.next()) {
            dataFound = true;
            String deskripsi = rs.getString("deskripsi");
            String saldo = rs.getString("saldo");

            // Buat label baru untuk deskripsi
            Label deskripsiLabel = new Label(deskripsi);
            deskripsiLabel.setFont(new Font(10));
            GridPane.setMargin(deskripsiLabel, new Insets(10, 0, 10, 10));

            // Buat label baru untuk saldo
            Label saldoLabel = new Label(saldo);
            saldoLabel.setFont(new Font(10));
            GridPane.setMargin(saldoLabel, new Insets(10, 0, 10, 10));

            // Tambahkan label ke dalam containerAsetTetap
            containerAsetTetap.add(deskripsiLabel, 0, rowIndex);
            containerAsetTetap.add(saldoLabel, 1, rowIndex);

            // Tambahkan row constraints untuk setiap baris baru
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(10);
            rowConstraints.setPrefHeight(30);
            rowConstraints.setVgrow(Priority.SOMETIMES);
            containerAsetTetap.getRowConstraints().add(rowConstraints);

            rowIndex++;
        }

        // Tambahkan line di kolom kedua sebelum label TOTAL AKTIVA TETAP
        if (dataFound) {
            Line line = new Line();
            line.setStartX(0.0);
            line.setEndX(100.0);
            containerAsetTetap.add(line, 1, rowIndex);

            RowConstraints lineRowConstraints = new RowConstraints();
            lineRowConstraints.setMinHeight(10);
            lineRowConstraints.setPrefHeight(30);
            lineRowConstraints.setVgrow(Priority.SOMETIMES);
            containerAsetTetap.getRowConstraints().add(lineRowConstraints);

            rowIndex++;

            // Tambahkan label TOTAL AKTIVA TETAP dengan bold
            Label totalLabel = new Label("TOTAL AKTIVA TETAP");
            totalLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            GridPane.setMargin(totalLabel, new Insets(10, 0, 10, 10));
            containerAsetTetap.add(totalLabel, 0, rowIndex);

            RowConstraints totalRowConstraints = new RowConstraints();
            totalRowConstraints.setMinHeight(10);
            totalRowConstraints.setPrefHeight(30);
            totalRowConstraints.setVgrow(Priority.SOMETIMES);
            containerAsetTetap.getRowConstraints().add(totalRowConstraints);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
