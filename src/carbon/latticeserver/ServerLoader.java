/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Simple Preloader Using the ProgressBar Control
 *
 * @author Aniket
 */
public class ServerLoader extends Preloader {

    ProgressBar bar;
    Stage stage;

    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        BorderPane p = new BorderPane();
        p.setStyle("-fx-background-color:gray");
        p.setCenter(bar);
        BorderPane ap = new BorderPane();
        p.setTop(ap);
        Label l, lk;
        ImageView im;
        bar.setPrefWidth(400);
        ap.setCenter(im = new ImageView(stage.getIcons().get(0)));
        im.setFitHeight(150);
        im.setFitWidth(150);
        im.setPreserveRatio(true);
        ap.setBottom(lk = new Label("Loading..."));
        lk.setStyle("-fx-text-fill:white;");
        lk.setFont(new Font(14));
        BorderPane.setAlignment(p.getTop(), Pos.BOTTOM_CENTER);
        BorderPane.setAlignment(ap.getBottom(), Pos.CENTER);
        BorderPane.setAlignment(p.getCenter(), Pos.CENTER);
        BorderPane.setAlignment(ap.getCenter(), Pos.CENTER);
        return new Scene(p, 500, 250, Color.LIGHTBLUE);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Code.png")));
        stage.setScene(createPreloaderScene());
        stage.setAlwaysOnTop(true);
        stage.setTitle("LatticeServer");
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification scn) {
        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
            ((new Thread(() -> {
                Platform.runLater(() -> {
                    bar.setProgress(1);
                    stage.hide();
                });
            }))).start();
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        if (pn.getProgress() > 0.1) {
            bar.setProgress(pn.getProgress() - 0.1);
        } else {
            bar.setProgress(pn.getProgress());
        }
    }

}
