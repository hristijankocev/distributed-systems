package mk.ukim.finki.lab_01.ccmp.exceptions;

public class MessageIntegrityViolationException extends RuntimeException {
    public MessageIntegrityViolationException(String message) {
        super(message);
    }
}
