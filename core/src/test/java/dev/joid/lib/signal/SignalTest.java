package dev.joid.lib.signal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.signal.impl.primitive.StringSignal;

public class SignalTest {

	@Test
	public void notifiesItsSubscribersOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null).subscribe(received::add);
		BridgeHandler.THREAD.register(thread);
		try {
			signal.set("ui");
			Assert.assertTrue(received.isEmpty());
			thread.run();
			Assert.assertEquals(Collections.singletonList("ui"), received);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void readsItsValueWithGetOrPeekOnly() {
		for (final Method method : ISignal.class.getMethods()) {
			Assert.assertNotEquals("getOrDefault", method.getName());
		}
	}

	@Test
	public void startsEmptyWithoutDefault() {
		final Signal<String> signal = Signal.of((String) null);
		Assert.assertNull(signal.get());
	}

	@Test
	public void startsWithTheGivenValue() {
		final Signal<String> signal = Signal.of("joid");
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals("joid", Signal.of("joid").set("ui").reset().get());
	}

	@Test
	public void keepsItsValueOverItsDefault() {
		final Signal<String> signal = Signal.of("joid").set("ui");
		Assert.assertEquals("ui", signal.get());
	}

	@Test
	public void resetsToItsDefault() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of("joid").set("ui").subscribe(received::add).reset();
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void forgetsItsValueOnResetWithoutDefault() {
		final Signal<String> signal = Signal.of((String) null).set("joid").reset();
		Assert.assertNull(signal.get());
	}

	@Test
	public void notifiesEveryChange() {
		final List<String> received = new ArrayList<>();
		Signal.of((String) null).subscribe(received::add).set("a").set("b");
		Assert.assertEquals(Arrays.asList("a", "b"), received);
	}

	@Test
	public void ignoresAnUnchangedValue() {
		final List<String> received = new ArrayList<>();
		Signal.of((String) null).subscribe(received::add).set(null).set("a").set(new String("a"));
		Assert.assertEquals(Collections.singletonList("a"), received);
	}

	@Test
	public void publishesItsValueOnDemand() {
		final List<String> received = new ArrayList<>();
		Signal.of("joid").subscribe(received::add).publish().publish();
		Assert.assertEquals(Arrays.asList("joid", "joid"), received);
	}

	@Test
	public void silencesOnlyTheNextPublish() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null).subscribe(received::add).silent().set("a");
		Assert.assertEquals("a", signal.get());
		Assert.assertTrue(received.isEmpty());
		signal.set("b");
		Assert.assertEquals(Collections.singletonList("b"), received);
	}

	@Test
	public void silencesAManualPublish() {
		final List<String> received = new ArrayList<>();
		Signal.of("joid").subscribe(received::add).silent().publish().publish();
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void exposesItsSubscribers() {
		final ISignalSubscriber<String> subscriber = value -> true;
		Assert.assertEquals(Collections.singleton(subscriber), Signal.of((String) null).subscribe(subscriber).getEventSet());
	}

	@Test
	public void notifiesASubscriberOnceWhenSubscribedTwice() {
		final List<String> received = new ArrayList<>();
		final ISignalSubscriber<String> subscriber = received::add;
		Signal.of((String) null).subscribe(subscriber).subscribe(subscriber).set("a");
		Assert.assertEquals(Collections.singletonList("a"), received);
	}

	@Test
	public void stopsNotifyingAnUnsubscribedSubscriber() {
		final List<String> received = new ArrayList<>();
		final ISignalSubscriber<String> subscriber = received::add;
		final Signal<String> signal = Signal.of((String) null).subscribe(subscriber).set("a").unsubscribe(subscriber).set("b");
		Assert.assertEquals(Collections.singletonList("a"), received);
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void dropsASubscriberReturningFalse() {
		final AtomicInteger calls = new AtomicInteger();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null).subscribe(value -> {
			calls.incrementAndGet();
			return false;
		}).subscribe(received::add).set("a").set("b");
		Assert.assertEquals(1, calls.get());
		Assert.assertEquals(Arrays.asList("a", "b"), received);
		Assert.assertEquals(1, signal.getEventSet().size());
	}

	@Test
	public void acceptsASubscriberDuringAPublish() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null);
		signal.subscribe(value -> {
			signal.subscribe(received::add);
			return false;
		}).set("a").set("b");
		Assert.assertEquals(Collections.singletonList("b"), received);
	}

	@Test
	public void readsItsValueOrItsDefault() {
		final Signal<String> signal = Signal.of("joid");
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals("joid", signal.peek());
		signal.set("ui");
		Assert.assertEquals("ui", signal.get());
		Assert.assertEquals("ui", signal.peek());
	}

	@Test
	public void isASupplier() {
		final Signal<String> signal = Signal.of("joid");
		final Supplier<String> supplier = signal;
		signal.set("ui");
		Assert.assertEquals("ui", supplier.get());
	}

	@Test
	public void notifiesOnceTheBatchEnds() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null).subscribe(received::add);
		Signal.batch(() -> {
			signal.set("a");
			signal.set("b");
			Signal.batch(() -> signal.set("c"));
			Assert.assertTrue(received.isEmpty());
		});
		Assert.assertEquals(Collections.singletonList("c"), received);
	}

	@Test
	public void notifiesTheChangesMadeByASubscriber() {
		final List<String> received = new ArrayList<>();
		final Signal<String> first = Signal.of((String) null);
		final Signal<String> second = Signal.of((String) null).subscribe(received::add);
		first.subscribe(value -> {
			second.set(value + "!");
			return true;
		}).set("a");
		Assert.assertEquals(Collections.singletonList("a!"), received);
	}

	@Test
	public void notifiesTheRestOfABatchAfterAFailingSubscriber() {
		final List<String> received = new ArrayList<>();
		final Signal<String> first = Signal.of((String) null).subscribe(value -> {
			throw new IllegalStateException();
		});
		final Signal<String> second = Signal.of((String) null).subscribe(received::add);
		try {
			Signal.batch(() -> {
				first.set("a");
				second.set("b");
			});
			Assert.fail();
		} catch (final IllegalStateException exception) {
			second.set("c");
		}
		Assert.assertEquals(Arrays.asList("b", "c"), received);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullSubscriber() {
		Signal.of((String) null).subscribe(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToUnsubscribeNull() {
		Signal.of((String) null).unsubscribe(null);
	}

	@Test
	public void takesTheValueOfACompletedStage() {
		final Signal<String> signal = Signal.of(CompletableFuture.completedFuture("joid"));
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void waitsForAPendingStage() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of(future).subscribe(received::add);
		Assert.assertNull(signal.get());
		future.complete("joid");
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void staysEmptyWhenTheStageFails() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Signal<String> signal = Signal.of(future);
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			future.completeExceptionally(new IllegalStateException("offline"));
		} finally {
			System.setErr(error);
		}
		Assert.assertNull(signal.get());
		Assert.assertEquals("[JOID] The future given to Signal.of(...) failed (java.lang.IllegalStateException: offline), the signal stays empty: handle the failure on the future, with exceptionally(...) for example" + System.lineSeparator(), new String(output.toByteArray(), StandardCharsets.UTF_8));
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullStage() {
		Signal.of((CompletionStage<String>) null);
	}

	@Test
	public void equalsOnlyItself() {
		final Signal<String> signal = Signal.of("joid");
		Assert.assertEquals(signal, signal);
		Assert.assertNotEquals(Signal.of("joid"), signal);
		Assert.assertNotEquals(Signal.of((String) null), Signal.of((String) null));
		Assert.assertNotEquals(StringSignal.of("joid"), StringSignal.of("joid"));
		Assert.assertEquals(System.identityHashCode(signal), signal.hashCode());
	}

	@Test
	public void disarmsItsSilenceOnAnUnchangedValue() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of((String) null).set("a").subscribe(received::add);
		signal.silent().set("a");
		signal.set("b");
		Assert.assertEquals(Collections.singletonList("b"), received);
	}

	@Test
	public void notifiesNullOnceItsValueIsCleared() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of("joid").set("ui").subscribe(received::add).set(null);
		Assert.assertNull(signal.get());
		Assert.assertEquals(Collections.singletonList(null), received);
		Assert.assertEquals("joid", signal.reset().get());
	}

	@Test
	public void notifiesNullWithoutDefault() {
		final List<String> received = new ArrayList<>();
		Signal.of((String) null).set("ui").subscribe(received::add).set(null);
		Assert.assertEquals(Collections.singletonList(null), received);
	}

}