package accountant.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AccountantSystem extends Application {

    private SessionManager sessionManager;

    @Override
    public void init() throws Exception {
        sessionManager = SessionManager.getInstance(); // Initialize session manager
//        database = Database.getInstance(); // Initialize database
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
//            database.resetDB(); // Reset DB (*ONLY WHEN U NEED*)
//            Database.hapusDB();
            Database.setupDatabase();
        } catch (Exception e) {
            System.out.println("Error setup database: " + e.getMessage());
        }
        Parent root;
        if (sessionManager.isLoggedIn()) {
            System.out.println("User already logged in.");
            root = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
        } else {
            root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        }
        
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
