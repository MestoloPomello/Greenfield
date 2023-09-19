package shared.exceptions;

public class DuplicatedIdException extends Exception {
    public DuplicatedIdException(String message) {
        super(message);
        System.err.println("[ERROR] " + message + ": this ID already exists.");
    }
    public DuplicatedIdException() { this("Generic DuplicatedIdException"); }
}
