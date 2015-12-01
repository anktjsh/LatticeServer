/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.latticeserver;

/**
 *
 * @author Aniket
 */
public class Password {

    private String username;
    private String password;
    private String question;
    private String answer;
    private boolean loggedIn;

    public Password() {
        this("", "", "", "");
    }

    public Password(String a, String b, String c, String d) {
        username = a;
        password = b;
        question =c;
        answer = d;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String save() {
        return username + "/" + password+"/" + question+"/" + answer;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Override
    public String toString() {
        return save();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Password) {
            Password pa = (Password) obj;
            if (pa.getUsername().equals(getUsername())) {
                if (pa.getPassword().equals(getPassword())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
