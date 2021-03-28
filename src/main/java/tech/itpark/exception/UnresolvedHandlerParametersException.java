package tech.itpark.exception;

public class UnresolvedHandlerParametersException extends RuntimeException {
    public UnresolvedHandlerParametersException() {
    }

    public UnresolvedHandlerParametersException(String message) {
        super(message);
    }

    public UnresolvedHandlerParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnresolvedHandlerParametersException(Throwable cause) {
        super(cause);
    }

    public UnresolvedHandlerParametersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
