/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

import carbon.lattice.Message;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Aniket
 */
public class LastMessage implements Serializable {

    private final String to, from;
    private final Message message;

    public LastMessage(String t, Message mes, String fr) {
        to = t;
        from = fr;
        message = mes;
    }

    public Object[] getSave() {
        return new Object[]{getTo(), getMessage(), getFrom()};
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public Message getMessage() {
        return message;
    }

    public static ArrayList<LastMessage> getMessages(ArrayList<Object> ob) {
        ArrayList<LastMessage> lm = new ArrayList<>();
        if (ob.size() % 3 == 0) {
            for (int x = 0; x < ob.size(); x += 3) {
                lm.add(new LastMessage((String) ob.get(x), (Message) ob.get(x + 1), (String) ob.get(x + 2)));
            }
        }
        return lm;
    }
}
