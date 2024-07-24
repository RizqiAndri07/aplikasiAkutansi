package accountant.system;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.util.Arrays;
import java.util.List;

public class neracaController {

    @FXML
    private ComboBox<String> bulan;
    @FXML
    private ComboBox<Integer> tahun;

    // List of months
    private final List<String> bulanList = Arrays.asList(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    );

    // List of years
    private final List<Integer> tahunList = Arrays.asList(
            2022, 2023, 2024, 2025, 2026
    );

    @FXML
    private void initialize() {
        // Initialize bulan ComboBox
        bulan.setItems(FXCollections.observableArrayList(bulanList));
        bulan.getSelectionModel().selectFirst(); // Select the first month by default

        // Initialize tahun ComboBox
        tahun.setItems(FXCollections.observableArrayList(tahunList));
        tahun.getSelectionModel().selectFirst(); // Select the first year by default
    }

    @FXML
    private void select() {
        String selectedBulan = bulan.getSelectionModel().getSelectedItem();
        Integer selectedTahun = tahun.getSelectionModel().getSelectedItem();
        System.out.println("Selected Month: " + selectedBulan);
        System.out.println("Selected Year: " + selectedTahun);
    }
}
