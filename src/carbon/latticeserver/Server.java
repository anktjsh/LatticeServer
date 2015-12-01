/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class Server extends Application {

    private static String publicIp;
    public static String getPublic() {
        return publicIp;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        server = new TreeSet<String>() {

            @Override
            public boolean add(String e) {
                if (e == null) {
                    return false;
                }
                return super.add(e);
            }

        };
        String s = getIp("http://curlmyip.com/");
        if (s != null) {
            server.add(s);
            publicIp = s;
        }
        String sa = getIp("http://checkip.amazonaws.com/");
        if (sa != null && !server.contains(sa)) {
            server.add(sa);
            if (sa != null && (!sa.equals(s))) {
                publicIp = sa;
            }
        }
        server.add(InetAddress.getLocalHost().getHostAddress());
        ServerStage ss = new ServerStage();
        ss.setTitle("Lattice Server");
        ss.setResizable(false);
        ss.getIcons().add(new Image(getClass().getResourceAsStream("Code.png")));
        ss.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static String redirectPort;

    public static String getPort() {
        return redirectPort;
    }

    static ArrayList<Component> removeDuplicates(ArrayList<Component> list) {
        ArrayList<Component> result = new ArrayList<>();
        HashSet<Component> set = new HashSet<>();
        for (Component item : list) {
            if (!set.contains(item)) {
                result.add(item);
                set.add(item);
            }
        }
        return result;
    }

    private static Set<String> server;

    public static Set<String> getServer() {
        return server;
    }

    private static String getIp(String s) {
        URL whatismyip = null;
        try {
            whatismyip = new URL(s);
        } catch (MalformedURLException ex) {
        }
        BufferedReader in = null;
        try {
            if (whatismyip != null) {
                in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
            }
        } catch (IOException ex) {
        }

        String ip = null;
        try {
            if (in != null) {
                ip = in.readLine();
            }
        } catch (IOException ex) {
        }
        return ip;
    }

}
