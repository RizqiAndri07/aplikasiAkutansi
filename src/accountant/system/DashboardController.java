/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package accountant.system;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Fania
 */
public class DashboardController implements Initializable {

    private SessionManager sessionManager;
    @FXML
    private AnchorPane anchorPane;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        sessionManager = SessionManager.getInstance();
        System.out.println("INI SESSION : " + sessionManager.getCurrentUserEmail() + sessionManager.getCurrentUserId());
        Platform.runLater(() -> {
           anchorPane.requestFocus();
        });
    }
}
