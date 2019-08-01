package io.smallrye.reactive.operators;

import io.smallrye.reactive.CompositeException;
import io.smallrye.reactive.TimeoutException;
import io.smallrye.reactive.Uni;
import io.smallrye.reactive.tuples.Pair;
import io.smallrye.reactive.tuples.Tuple3;
import io.smallrye.reactive.tuples.Tuple4;
import io.smallrye.reactive.tuples.Tuple5;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class UniZipTest {

    @Test
    public void testWithTwoSimpleUnis() {

        Uni<Integer> uni = Uni.createFrom().result(1);
        Uni<Integer> uni2 = Uni.createFrom().result(2);

        UniAssertSubscriber<Pair<Integer, Integer>> subscriber =
                Uni.combine().all().unis(uni, uni2).asPair().subscribe().withSubscriber(UniAssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 2);
    }

    @Test
    public void testWithTwoOneFailure() {
        Uni<Integer> uni = Uni.createFrom().result(1);
        UniAssertSubscriber<Pair<Integer, Integer>> subscriber =
                Uni.combine().all().unis(uni, Uni.createFrom().<Integer>failure(new IOException("boom"))).asPair()
                        .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertFailure(IOException.class, "boom");
    }

    @Test(expected = TimeoutException.class)
    public void testWithNever() {
        Uni<Tuple3<Integer, Integer, Object>> tuple = Uni.combine().all().unis(Uni.createFrom().result(1),
                Uni.createFrom().result(2), Uni.createFrom().nothing()).asTuple();
        tuple.await().atMost(Duration.ofMillis(1000));
    }

    @Test
    public void testWithTwoFailures() {
        UniAssertSubscriber<Tuple3<Integer, Integer, Integer>> subscriber =
                Uni.combine().all().unis(
                        Uni.createFrom().result(1),
                        Uni.createFrom().<Integer>failure(new IOException("boom")),
                        Uni.createFrom().<Integer>failure(new IOException("boom 2")))
                        .collectFailures()
                        .asTuple()
                        .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompletedWithFailure()
                .assertFailure(CompositeException.class, "boom")
                .assertFailure(CompositeException.class, "boom 2");
    }

    @Test
    public void testWithCombinator() {
        Uni<Integer> uni1 = Uni.createFrom().result(1);
        Uni<Integer> uni2 = Uni.createFrom().result(2);
        Uni<Integer> uni3 = Uni.createFrom().result(3);
        UniAssertSubscriber<Integer> subscriber =
                Uni.combine().all().unis(uni1, uni2, uni3)
                        .combinedWith((i1, i2, i3) -> i1 + i2 + i3)
                        .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.await().assertResult(6);
    }

    @Test
    public void testTerminationJoin() {
        Uni<Void> uni = Uni.combine().all().unis(Uni.createFrom().result(1),
                Uni.createFrom().result("hello")).asPair().onResult().ignoreIt().andContinueWithNull();

        uni.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompletedSuccessfully()
                .assertResult(null);
    }

    @Test
    public void testWithFiveUnis() {
        Uni<Integer> uni = Uni.createFrom().result(1);
        Uni<Integer> uni2 = Uni.createFrom().result(2);
        Uni<Integer> uni3 = Uni.createFrom().result(3);
        Uni<Integer> uni4 = Uni.createFrom().result(4);

        UniAssertSubscriber<Tuple5<Integer, Integer, Integer, Integer, Integer>> subscriber =
                Uni.combine().all().unis(uni, uni, uni2, uni3, uni4).asTuple().subscribe().withSubscriber(UniAssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 1, 2, 3, 4);
    }

    @Test
    public void testWithFourUnis() {
        Uni<Integer> uni = Uni.createFrom().result(1);
        Uni<Integer> uni2 = Uni.createFrom().result(2);
        Uni<Integer> uni3 = Uni.createFrom().result(3);

        UniAssertSubscriber<Tuple4<Integer, Integer, Integer, Integer>> subscriber =
                Uni.combine().all().unis(uni, uni, uni2, uni3).asTuple().subscribe().withSubscriber(UniAssertSubscriber.create());

        assertThat(subscriber.getResult().asList()).containsExactly(1, 1, 2, 3);
    }


}