package exception;

public class DuplicateSlotException extends RuntimeException {
    public DuplicateSlotException(String message) {
        super(message);
    }
}