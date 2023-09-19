package shared.exceptions;

public class UnrecognisedMessageException extends Exception {
    public UnrecognisedMessageException(String message) {
        super(message);
        System.err.println("[ERROR] " + message + ": unrecognised robot message.");
    }
    public UnrecognisedMessageException() { this("Generic UnrecognisedMessageException"); }
}
