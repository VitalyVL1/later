package ru.practicum.common;

public class ItemRetrieverException extends RuntimeException {
    public ItemRetrieverException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemRetrieverException(String message) {
        super(message);
    }
}
