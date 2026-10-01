package standart.code.error;

public class QueueOverflow extends RuntimeException {
    public QueueOverflow(String message) {
        super(message);
    }
}
