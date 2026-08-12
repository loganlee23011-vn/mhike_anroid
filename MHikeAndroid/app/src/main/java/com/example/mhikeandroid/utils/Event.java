package com.example.mhikeandroid.utils;

/**
 * Wraps a one-shot LiveData payload (navigation triggers, error/success messages) so it is
 * only acted on once, even across configuration changes that re-deliver the last LiveData
 * value to a fresh observer.
 */
public class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /** Returns the content and marks it handled, or null if already handled. */
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
