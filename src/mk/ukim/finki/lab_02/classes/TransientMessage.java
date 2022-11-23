package mk.ukim.finki.lab_02.classes;


public class TransientMessage {
    private final Client from;
    private final Client to;
    private final String messageContent;

    public TransientMessage(Client from, Client to, String messageContent) {
        this.from = from;
        this.to = to;
        this.messageContent = messageContent;
    }

    public Client getFrom() {
        return from;
    }

    public Client getTo() {
        return to;
    }

    public String getMessageContent() {
        return messageContent;
    }
}
