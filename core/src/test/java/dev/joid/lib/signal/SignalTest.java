package dev.joid.lib.signal;

import java.lang.reflect.Method;
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
		final Signal<String> signal = new Signal<String>().subscribe(received::add);
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
		final Signal<String> signal = new Signal<>();
		Assert.assertFalse(signal.isPresent());
		Assert.assertNull(signal.get());
	}

	@Test
	public void fallsBackOnItsDefault() {
		final Signal<String> signal = new Signal<>("joid");
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void startsWithTheGivenValue() {
		final Signal<String> signal = Signal.of("joid");
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals("joid", Signal.of("joid").set("ui").reset().get());
	}

	@Test
	public void keepsItsValueOverItsDefault() {
		final Signal<String> signal = new Signal<>("joid").set("ui");
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals("ui", signal.get());
	}

	@Test
	public void resetsToItsDefault() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<>("joid").set("ui").subscribe(received::add).reset();
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void forgetsItsValueOnResetWithoutDefault() {
		final Signal<String> signal = new Signal<String>().set("joid").reset();
		Assert.assertFalse(signal.isPresent());
		Assert.assertNull(signal.get());
	}

	@Test
	public void notifiesEveryChange() {
		final List<String> received = new ArrayList<>();
		new Signal<String>().subscribe(received::add).set("a").set("b");
		Assert.assertEquals(Arrays.asList("a", "b"), received);
	}

	@Test
	public void ignoresAnUnchangedValue() {
		final List<String> received = new ArrayList<>();
		new Signal<String>().subscribe(received::add).set(null).set("a").set(new String("a"));
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
		final Signal<String> signal = new Signal<String>().subscribe(received::add).silent().set("a");
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
		Assert.assertEquals(Collections.singleton(subscriber), new Signal<String>().subscribe(subscriber).getEventSet());
	}

	@Test
	public void notifiesASubscriberOnceWhenSubscribedTwice() {
		final List<String> received = new ArrayList<>();
		final ISignalSubscriber<String> subscriber = received::add;
		new Signal<String>().subscribe(subscriber).subscribe(subscriber).set("a");
		Assert.assertEquals(Collections.singletonList("a"), received);
	}

	@Test
	public void stopsNotifyingAnUnsubscribedSubscriber() {
		final List<String> received = new ArrayList<>();
		final ISignalSubscriber<String> subscriber = received::add;
		final Signal<String> signal = new Signal<String>().subscribe(subscriber).set("a").unsubscribe(subscriber).set("b");
		Assert.assertEquals(Collections.singletonList("a"), received);
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void dropsASubscriberReturningFalse() {
		final AtomicInteger calls = new AtomicInteger();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<String>().subscribe(value -> {
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
		final Signal<String> signal = new Signal<>();
		signal.subscribe(value -> {
			signal.subscribe(received::add);
			return false;
		}).set("a").set("b");
		Assert.assertEquals(Collections.singletonList("b"), received);
	}

	@Test
	public void readsItsValueOrItsDefault() {
		final Signal<String> signal = new Signal<>("joid");
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
		final Signal<String> signal = new Signal<String>().subscribe(received::add);
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
		final Signal<String> first = new Signal<>();
		final Signal<String> second = new Signal<String>().subscribe(received::add);
		first.subscribe(value -> {
			second.set(value + "!");
			return true;
		}).set("a");
		Assert.assertEquals(Collections.singletonList("a!"), received);
	}

	@Test
	public void notifiesTheRestOfABatchAfterAFailingSubscriber() {
		final List<String> received = new ArrayList<>();
		final Signal<String> first = new Signal<String>().subscribe(value -> {
			throw new IllegalStateException();
		});
		final Signal<String> second = new Signal<String>().subscribe(received::add);
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
		new Signal<String>().subscribe(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToUnsubscribeNull() {
		new Signal<String>().unsubscribe(null);
	}

	@Test
	public void takesTheValueOfACompletedStage() {
		final Signal<String> signal = Signal.of(CompletableFuture.completedFuture("joid"));
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void waitsForAPendingStage() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = Signal.of(future).subscribe(received::add);
		Assert.assertFalse(signal.isPresent());
		future.complete("joid");
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void staysEmptyWhenTheStageFails() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Signal<String> signal = Signal.of(future);
		future.completeExceptionally(new IllegalStateException());
		Assert.assertFalse(signal.isPresent());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullStage() {
		Signal.of((CompletionStage<String>) null);
	}

	@Test
	public void equalsASignalHoldingTheSameValue() {
		Assert.assertEquals(Signal.of("joid"), Signal.of("joid"));
		Assert.assertEquals(Signal.of("joid").hashCode(), Signal.of("joid").hashCode());
		Assert.assertEquals(new Signal<String>(), new Signal<String>());
		Assert.assertEquals(new Signal<String>().hashCode(), new Signal<String>().hashCode());
	}

	@Test
	public void comparesTheValueItWouldGive() {
		Assert.assertEquals(new Signal<>("joid"), Signal.of("joid"));
		Assert.assertEquals(new Signal<>("joid").hashCode(), Signal.of("joid").hashCode());
		Assert.assertNotEquals(new Signal<>("joid"), new Signal<>("ui"));
	}

	@Test
	public void differsFromAnotherValueOrAnotherKind() {
		final Signal<String> signal = Signal.of("joid");
		Assert.assertEquals(signal, signal);
		Assert.assertNotEquals(Signal.of("ui"), signal);
		Assert.assertNotEquals(new Signal<String>(), signal);
		Assert.assertNotEquals(StringSignal.of("joid"), signal);
		Assert.assertNotEquals(signal, null);
	}

	@Test
	public void disarmsItsSilenceOnAnUnchangedValue() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<String>().set("a").subscribe(received::add);
		signal.silent().set("a");
		signal.set("b");
		Assert.assertEquals(Collections.singletonList("b"), received);
	}

	@Test
	public void notifiesItsDefaultOnceItsValueIsCleared() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<>("joid").set("ui").subscribe(received::add).set(null);
		Assert.assertEquals("joid", signal.get());
		Assert.assertEquals(Collections.singletonList("joid"), received);
	}

	@Test
	public void notifiesNullWithoutDefault() {
		final List<String> received = new ArrayList<>();
		new Signal<String>().set("ui").subscribe(received::add).set(null);
		Assert.assertEquals(Collections.singletonList(null), received);
	}

}