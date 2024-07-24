package accountant.system;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {

    public static void showToast(Stage ownerStage, String message, String type) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            Label label = new Label(message);
            label.getStyleClass().add("toast");
            label.getStyleClass().add(type);

            StackPane content = new StackPane(label);
            content.getStylesheets().add(Toast.class.getResource("toast.css").toExternalForm());
            content.setOpacity(0);

            popup.getContent().add(content);
            popup.show(ownerStage);

            popup.setY(ownerStage.getY() + 30); // Sesuaikan nilai ini sesuai kebutuhan
            popup.setX(ownerStage.getX() + (ownerStage.getWidth() - content.getWidth()) / 2); // Pusatkan secara horizontal

            Timeline fadeInTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(content.opacityProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(content.opacityProperty(), 1))
            );

            Timeline fadeOutTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), new KeyValue(content.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(2.5), new KeyValue(content.opacityProperty(), 0))
            );

            fadeOutTimeline.setOnFinished(event -> popup.hide());

            fadeInTimeline.setOnFinished(event -> fadeOutTimeline.play());
            fadeInTimeline.play();
        });
    }

    public static void showConfirmationPopup(Stage ownerStage, String title, String contentText, Runnable onYes, Runnable onCancel) {
        Platform.runLater(() -> {
            // Membuat dialog baru
            Stage dialog = new Stage();
            dialog.initOwner(ownerStage);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.initModality(Modality.WINDOW_MODAL);  // Membuat dialog modal

            // Label judul
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("toast-title");
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            titleLabel.setAlignment(Pos.CENTER_LEFT);

            // Label konten
            Label contentLabel = new Label(contentText);
            contentLabel.getStyleClass().add("toast-content");

            // Tombol ya
            Button yesButton = new Button("Ya");
            yesButton.getStyleClass().add("toast-button");

            // Tombol batal
            Button cancelButton = new Button("Batal");
            cancelButton.getStyleClass().addAll("toast-button", "toast-cancel-button");

            // Tindakan tombol
            yesButton.setOnAction(event -> {
                dialog.close();
                if (onYes != null) {
                    onYes.run();
                }
            });

            cancelButton.setOnAction(event -> {
                dialog.close();
                if (onCancel != null) {
                    onCancel.run();

                }
            });

            // Layout tombol
            HBox buttonBox = new HBox(3, yesButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 10, 10, 0));

            // Layout konten
            VBox contentBox = new VBox(10, titleLabel, contentLabel, buttonBox);
//            contentBox.setPadding(new Insets(10));
            contentBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; -fx-border-radius: 5px; -fx-background-radius: 5px;");

            Scene scene = new Scene(contentBox);
            scene.getStylesheets().add(Toast.class.getResource("toast.css").toExternalForm());

            dialog.setScene(scene);

            // Menampilkan dialog di tengah jendela induk
            dialog.setX(ownerStage.getX() + (ownerStage.getWidth() / 2 - contentBox.getWidth()) / 2 - 20);
            dialog.setY(ownerStage.getY() + (ownerStage.getHeight() / 2 - contentBox.getHeight()) / 2 + 20);
            
            dialog.showAndWait();  // Menampilkan dialog dan menunggu hingga ditutup
        });
    }
}
