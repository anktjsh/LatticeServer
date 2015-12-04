/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

import carbon.lattice.Message;
import com.daexsys.grappl.client.api.Grappl;
import com.daexsys.grappl.client.api.GrapplBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;

/**
 *
 * @author Aniket
 */
public class ServerPane extends BorderPane {

    private final ArrayList<Message> messages;
//    private final TextArea area;
    private final ListView<String> view;
    private final MenuBar bar;
    private static final int PORT = 16384;
    private ServerSocket ss;
    private final Text data;
    private final ArrayList<Connection> conn;
    private Grappl gra;
    static final ArrayList<LastMessage> last = new ArrayList<>();
    static final ArrayList<Password> passwords = new ArrayList<Password>() {

        @Override
        public boolean add(Password e) {
            if (contains(e)) {
                return false;
            }
            return super.add(e); //To change body of generated methods, choose Tools | Templates.
        }

    };

    public ServerPane() {
        view = new ListView<>();
        setCenter(view);
        view.setMinSize(400, 400);
        bar = new MenuBar();
        setTop(bar);
        conn = new ArrayList<>();
        bar.getMenus().addAll(new Menu("Connection"), new Menu("Storage"));
        bar.getMenus().get(0).getItems().add(new MenuItem("Start"));
        bar.getMenus().get(0).getItems().get(0).setOnAction((e) -> {
            if (ss == null) {
                boolean b = false;
                Alert al = new Alert(AlertType.CONFIRMATION);
                al.setTitle("Grappl");
                al.setHeaderText("Active Grappl?");
                al.initModality(Modality.APPLICATION_MODAL);
                al.initOwner(getScene().getWindow());
                Optional<ButtonType> show = al.showAndWait();
                if (show.isPresent()) {
                    if (show.get() == ButtonType.OK || show.get() == ButtonType.YES) {
                        b = true;
                    }
                }
                final boolean k = b;
                (new Thread(() -> {
                    try {
                        if (k) {
                            gra = new GrapplBuilder().atLocalPort(PORT).build();
                            appendText("connect");
                            gra.connect("p.grappl.io");
                            while (gra.getExternalPort() == null) {
                                appendText("reconnect");
                                gra.restart();
                            }
                            appendText("p.grappl.io:" + gra.getExternalPort());
                            new Thread(new Data()).start();
                        }
                        appendText("Activating Server");
                        ss = new ServerSocket(PORT);
                        System.out.println(ss.isBound() + " " + ss.isClosed());
                        while (!ss.isClosed()) {
                            Socket s = ss.accept();
                            ObjectOutputStream iis = new ObjectOutputStream(s.getOutputStream());
                            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

                            String ser = s.getInetAddress().getHostAddress();
                            appendText("" + ser + "");
                            Connection c = new Connection(ois, iis, conn.size());
                            conn.add(c);
                            (new Thread(c)).start();
                        }
                    } catch (IOException fe) {
                    }
                })).start();
                bar.getMenus().get(0).getItems().get(0).setDisable(true);
                appendText("" + "Server IP : " + Server.getServer() + "");
            }
        });
        bar.getMenus().get(1).getItems().addAll(new MenuItem("Clear"));
        messages = new ArrayList<>();
        bar.getMenus().get(1).getItems().get(0).setOnAction((e) -> {
            try {
                Files.delete(new File("save.txt").toPath());
            } catch (IOException ex) {
            }
            messages.clear();
        });
        
        load();
        for (Password p : passwords) {
            appendText(p.toString());
        }
        data = new Text("");
        setBottom(data);
        BorderPane.setAlignment(data, Pos.CENTER);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save();
            } catch (IOException ex) {
            }
        }));
    }

    private void save() throws IOException {
        File f = new File("save.txt");
        try (FileOutputStream fout = new FileOutputStream(f); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            for (Message m : messages) {
                oos.writeObject(m);
            }
        }
        f = new File("last.txt");
        try (FileOutputStream fout = new FileOutputStream(f); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            for (LastMessage m : last) {
                oos.writeObject(m);
            }
        }
    }

    private void load() {
        File f = new File("people.txt");
        ArrayList<String> ast = new ArrayList<>();
        try {
            ast.addAll(Files.readAllLines(f.toPath()));
        } catch (IOException ex) {
        }
        for (String s : ast) {
            //appendText(s);
            String spl[] = s.split("/");
            ///appendText(Arrays.toString(spl));
            if (spl.length == 4) {
                passwords.add(new Password(spl[0], spl[1], spl[2], spl[3]));
            }
        }
        appendText(passwords.size() + "");

        f = new File("save.txt");
        try (FileInputStream fout = new FileInputStream(f); ObjectInputStream oos = new ObjectInputStream(fout)) {
            Object in;
            while ((in = oos.readObject()) != null) {
                messages.add((Message) in);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException | ClassNotFoundException ex) {
        }
        appendText(messages.size() + "");

        f = new File("last.txt");
        try (FileInputStream fout = new FileInputStream(f); ObjectInputStream oos = new ObjectInputStream(fout)) {
            Object in;
            while ((in = oos.readObject()) != null) {
                last.add((LastMessage) in);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException | ClassNotFoundException ex) {
        }
        System.out.println(last.size());
    }

    private class Data implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                Platform.runLater(() -> {
                    data.setText(gra.getStatsManager().getReceivedData() + "");
                });
            }
        }
    }

    public void close() throws IOException {
        if (ss != null && !ss.isClosed()) {
            ss.close();
        }
    }

    class Connection implements Runnable {

        private final ObjectOutputStream oos;
        private final ObjectInputStream ois;
        private int index;
        private final Password pass;

        public Connection(ObjectInputStream ois, ObjectOutputStream oos, int index) {
            this.oos = oos;
            this.ois = ois;
            this.index = index;
            pass = new Password();
        }

        public String getUsername() {
            return pass.getUsername();
        }

        public String getPassword() {
            return pass.getPassword();
        }

        public Password getPass() {
            return pass;
        }

        public int getIndex() {
            return index;
        }

        public void reduceIndex() {
            index--;
        }

        public ObjectOutputStream getOOS() {
            return oos;
        }

        @Override
        public void run() {
            try {
                Object oj;
                while ((oj = ois.readObject()) != null) {
                    if (oj instanceof Message) {
                        Message m = (Message) oj;
                        messages.add(m);
                        appendText(m + "");
                        Connection c = getRecipient(m.getTo(), index);
                        if (c != null) {
                            appendText(c.getUsername() + c.getPassword());
                            c.getOOS().writeObject(m);
                            getOOS().writeObject(0);
                            appendText("write 0");
                        } else {
                            getOOS().writeObject(-1);
                            appendText("write -1");
                        }
                    } else if (oj instanceof ArrayList) {
                        ArrayList str = (ArrayList) oj;
                        String one = (String) str.get(0);
                        appendText("" + str + "");
                        Connection c = getRecipient(one, index);
                        if (c != null) {
                            c.getOOS().writeObject(oj);
                            oos.writeObject(0);
                        } else {
                            oos.writeObject(-1);
                        }
                    } else if (oj instanceof String[]) {
                        appendText("" + "Verify");
                        String[] temp = (String[]) oj;
                        appendText("" + Arrays.toString(temp));
                        if (temp.length == 3) {
                            oos.writeObject(confirmCredentials(temp[0], temp[1], temp[2], pass));
                        } else if (temp.length == 1) {
                            oos.writeObject(getRecipients(temp[0]));
                        } else if (temp.length == 2) {
                            if (temp[0].equals("Security")) {
                                String st = getQuestion(temp[1], pass, passwords);
                                appendText(st);
                                oos.writeObject(st);
                            } else {
                                Message m = getLastMessage(temp[0], temp[1]);
                                appendText(m.getText() + " " + m.getMetadata());
                                oos.writeObject(m);
                            }
                        } else if (temp.length == 4) {
                            String st = getRecoveredPassword(temp[1], temp[2], temp[3], pass);
                            appendText(st);
                            oos.writeObject(st);
                        } else if (temp.length == 5) {
                            oos.writeObject(confirmRegister(temp[0], temp[1], temp[2], temp[3], temp[4], pass));
                        }
                    } else if (oj instanceof Object[]) {
                        Object[] temp = (Object[]) oj;
                        if (temp.length == 4) {
                            List<Message> me = getMessages((String) temp[1], (String) temp[2], (Message) temp[3]);
                            appendText("size" + me.size());
                            oos.writeObject(me);
                        } else if (temp.length == 3) {
                            addLast((String) temp[0], (Message) temp[1], (String) temp[2]);
                            appendText("Add last" + Arrays.toString(temp));
                            Message m = (Message) temp[1];
                            appendText(m.getText() + " " + m.getMetadata());
                        }
                    } else if (oj instanceof String) {
                        String str = (String) oj;
                        boolean con = containsUsername(str, passwords);
                        appendText(con + "");
                        oos.writeObject(con);
                    }
                }
            } catch (IOException ex) {
                conn.remove(this);
                getPass().setLoggedIn(false);
                appendText("Remove : " + getUsername());
                if (passwords.contains(getPass())) {
                    passwords.get(passwords.indexOf(pass)).setLoggedIn(false);
                }
                for (Connection c : conn) {
                    if (c.getIndex() > getIndex()) {
                        c.reduceIndex();
                    }
                }
            } catch (ClassNotFoundException e) {
            }
        }
    }

    public Message getLastMessage(String to, String from) {
        for (LastMessage lm : last) {
            if (lm.getTo().equals(to) && lm.getFrom().equals(from)) {
                return lm.getMessage();
            }
        }
        return new Message(null, null, Message.TEXT, null, null, null, null);
    }

    public void addLast(String to, Message me, String from) {
        boolean found = false;
        for (int x = last.size() - 1; x >= 0; x--) {
            if (last.get(x).getTo().equals(to) && last.get(x).getFrom().equals(from)) {
                last.set(x, new LastMessage(to, me, from));
                found = true;
            }
        }
        if (!found) {
            last.add(new LastMessage(to, me, from));
        }
    }

    public ArrayList<String> getRecipients(String s) {
        ArrayList<String> str = new ArrayList<>();
        appendText(System.currentTimeMillis() + "");
        for (Message m : messages) {
            if (m.getTo().equals(s)) {
                if (!str.contains(m.getFrom())) {
                    str.add(m.getFrom());
                }
            } else if (m.getFrom().equals(s)) {
                if (!str.contains(m.getTo())) {
                    str.add(m.getTo());
                }
            }
        }
        appendText(System.currentTimeMillis() + "");
        appendText(str.toString());
        if (str.contains(s)) {
            str.remove(s);
        }
        return str;
    }

    public String getRecoveredPassword(String user, String ques, String asn, Password pass) {
        if (pass.getUsername().contains(user)) {
            if (pass.getQuestion().equals(ques)) {
                if (pass.getAnswer().equals(asn)) {
                    return pass.getPassword();
                }
            }
        }
        return "";
    }

    public String getQuestion(String username, Password pass, ArrayList<Password> pas) {
        if (!pass.getUsername().isEmpty()) {
            if (pass.getUsername().equals(username)) {
                return pass.getQuestion();
            }
        } else {
            for (Password p : pas) {
                if (p.getUsername().equals(username)) {
                    pass.setUsername(p.getUsername());
                    pass.setPassword(p.getPassword());
                    pass.setQuestion(p.getQuestion());
                    pass.setAnswer(p.getAnswer());
                    return p.getQuestion();
                }
            }
        }
        return "";
    }

    public boolean containsUsername(String str, ArrayList<Password> app) {
        for (Password p : app) {
            if (p.getUsername().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Message> getMessages(String a, String b, Message c) {
        ArrayList<Message> mes = new ArrayList<>();
        for (Message m : messages) {
            if ((m.getTo().equals(a) && m.getFrom().equals(b)) || (m.getTo().equals(b) && m.getFrom().equals(a))) {
                mes.add(m);
            }
        }
        Collections.sort(mes);
        int n = getMessage(mes, c);
        System.out.println(c);
        System.out.println(n);
        System.out.println("mes" + mes.size());
        if (n == -1) {
            return mes;
        } else {
            ArrayList<Message> aj = new ArrayList<>();
            System.out.println(c.getText());
            if (n + 1 == mes.size()) {
                return aj;
            }
            System.out.println(mes.get(n + 1).getText());
            aj.addAll(mes.subList(n + 1, mes.size()));
            return aj;
        }
    }

    public int getMessage(List<Message> mes, Message m) {
        System.out.println(mes.size());
        for (int x = mes.size() - 1; x >= 0; x--) {
            if (mes.get(x).getTo().equals(m.getTo())) {
                if (mes.get(x).getFrom().equals(m.getFrom())) {
                    if (mes.get(x).getText().equals(m.getText())) {
                        if (mes.get(x).getMetadata().equals(m.getMetadata())) {
                            if (mes.get(x).getTimeSent().equals(m.getTimeSent())) {
                                return x;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean confirmRegister(String a, String b, String c, String d, String e, Password pass) {
        if (a.equals("Register")) {
            for (Password p : passwords) {
                if (p.getUsername().equals(b)) {
                    return false;
                }
            }
            //EDIT
            pass.setUsername(b);
            pass.setQuestion(d);
            pass.setAnswer(e);
            pass.setPassword(c);
            passwords.add(pass);
            return true;
        }
        return false;
    }

    public boolean confirmCredentials(String a, String b, String c, Password pass) {
        if (a.equals("Login")) {
            if (!pass.getUsername().isEmpty() && !pass.getPassword().isEmpty()) {
                if (pass.getUsername().equals(b) && pass.getPassword().equals(c) && !pass.isLoggedIn()) {
                    if (!passwords.contains(pass)) {
                        passwords.add(pass);
                    } else {
                        passwords.get(passwords.indexOf(pass)).setLoggedIn(true);
                    }
                    pass.setLoggedIn(true);
                    return true;
                } else {
                    return false;
                }
            } else {
                for (Password p : passwords) {
                    if (p.getUsername().equals(b) && p.getPassword().equals(c) && !p.isLoggedIn()) {
                        pass.setUsername(p.getUsername());
                        pass.setPassword(p.getPassword());
                        p.setLoggedIn(true);
                        pass.setLoggedIn(true);
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private void appendText(String s) {
        Platform.runLater(() -> {
            view.getItems().add(s);
        });
    }

    ArrayList<Connection> getPeople() {
        return conn;
    }

    public String getLog() {
        String sa = "";
        for (String s : view.getItems()) {
            sa += s + "";
        }
        return sa;
    }

    private Connection getRecipient(String user, int index) {
        for (int x = 0; x < conn.size(); x++) {
            if (x != index) {
                if (conn.get(x).getUsername() != null && conn.get(x).getUsername().equals(user)) {
                    return conn.get(x);
                }
            }
        }
        return null;
    }
}
