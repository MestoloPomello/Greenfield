package shared.exceptions;

public class NoIdException extends Exception {
    public NoIdException(String message) {
        super(message);
        System.err.println("[ERROR] " + message + ": this ID doesn't exist.");
    }
    public NoIdException() {
        this("Generic NoIdException");
    }
}
