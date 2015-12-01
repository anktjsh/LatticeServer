/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.JFrame;

/**
 *
 * @author Aniket
 */
public class ServerStage extends Stage {

    private final ServerPane ss;

    public ServerStage() {
        setScene(new Scene(ss = new ServerPane()));
        setOnCloseRequest((e) -> {
            //WRITE PEOPLE TO FILE
            File fa = new File("people.txt");
            Collection<String> ala = new TreeSet<>();
            if (fa.exists()) {
                try {
                    ala.addAll(Files.readAllLines(fa.toPath()));
                } catch (IOException ex) {
                }
            }
            for (Password c : ServerPane.passwords) {
                ala.add(c.save());
            }
            try {
                Files.write(fa.toPath(), ala);
            } catch (IOException ex) {
            }

            try {
                ss.close();
            } catch (IOException ex) {
            }
            ArrayList<String> al = new ArrayList<>();
            al.addAll(Arrays.asList(ss.getLog().split("\n")));
            File f = new File("log" + LocalDate.now().toString().replaceAll(":", ".") + LocalTime.now().toString().replaceAll(":", ".") + ".txt");
            try {
                Files.write(f.toPath(), al);
            } catch (IOException ex) {
            }

            Platform.exit();
            System.exit(0);
        });
    }
}
