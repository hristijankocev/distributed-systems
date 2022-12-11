package mk.ukim.finki.lab_02.classes;

import java.io.Serializable;

public class Message implements Serializable {
    private String content;
    private Client client;

    public Message() {
    }

    public Message(String content, Client client) {
        this.content = content;
        this.client = client;
    }

    public String getContent() {
        return content;
    }

    public Client getClient() {
        return client;
    }

}
