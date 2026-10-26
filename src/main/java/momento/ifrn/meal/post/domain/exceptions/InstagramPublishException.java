package momento.ifrn.meal.post.domain.exceptions;

public class InstagramPublishException extends RuntimeException {
    public InstagramPublishException(final String message) {
        super(message);
    }

    public InstagramPublishException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
