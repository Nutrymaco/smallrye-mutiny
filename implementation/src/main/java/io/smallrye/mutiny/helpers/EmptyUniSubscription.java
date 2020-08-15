package io.smallrye.mutiny.helpers;

import io.smallrye.mutiny.subscription.UniSubscriber;
import io.smallrye.mutiny.subscription.UniSubscription;

/**
 * An implementation of {@link UniSubscription} ignoring all call to {@link #cancel()}.
 * This implementation should be accessed using the {@link #CANCELLED} instance.
 */
public class EmptyUniSubscription implements UniSubscription {

    /**
     * The instance that can be shared.
     * Calling {@link #cancel()} is a no-op.
     */
    public static final UniSubscription CANCELLED = new EmptyUniSubscription();

    private EmptyUniSubscription() {
        // Avoid direct instantiation.
    }

    /**
     * Propagates a failure to the given downstream subscriber.
     * The subscriber receive the {@code CANCELLED} subscription followed with the failure.
     *
     * @param subscriber the subscriber, must not be {@code null}
     * @param failure the failure, must not be {@code null}
     * @param <T> the expected item type
     */
    public static <T> void propagateFailureEvent(UniSubscriber<T> subscriber, Throwable failure) {
        subscriber.onSubscribe(CANCELLED);
        subscriber.onFailure(failure);
    }

    @Override
    public void cancel() {
        // Ignored.
    }
}
