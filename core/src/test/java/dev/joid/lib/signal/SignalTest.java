package dev.joid.lib.signal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.utils.thread.ThreadUtils;

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
			thread.renderThread(true).run();
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
		Assert.assertNull(signal.get());
	}

	@Test
	public void startsWithTheGivenValue() {
		final Signal<String> signal = new Signal<>("joid");
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals("joid", new Signal<>("joid").set("ui").reset().get());
	}

	@Test
	public void keepsItsValueOverItsDefault() {
		final Signal<String> signal = new Signal<>("joid").set("ui");
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
		new Signal<>("joid").subscribe(received::add).publish().publish();
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
		new Signal<>("joid").subscribe(received::add).silent().publish().publish();
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
		final Signal<String> signal = new Signal<>("joid");
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
	public void equalsOnlyItself() {
		final Signal<String> signal = new Signal<>("joid");
		Assert.assertEquals(signal, signal);
		Assert.assertNotEquals(new Signal<>("joid"), signal);
		Assert.assertNotEquals(new Signal<String>(), new Signal<String>());
		Assert.assertNotEquals(new StringSignal("joid"), new StringSignal("joid"));
		Assert.assertEquals(System.identityHashCode(signal), signal.hashCode());
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
	public void notifiesNullOnceItsValueIsCleared() {
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<>("joid").set("ui").subscribe(received::add).set(null);
		Assert.assertNull(signal.get());
		Assert.assertEquals(Collections.singletonList(null), received);
		Assert.assertEquals("joid", signal.reset().get());
	}

	@Test
	public void notifiesNullWithoutDefault() {
		final List<String> received = new ArrayList<>();
		new Signal<String>().set("ui").subscribe(received::add).set(null);
		Assert.assertEquals(Collections.singletonList(null), received);
	}

	@Test
	public void appliesAWriteFromAnotherThreadAtTheNextFrame() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final Signal<String> signal = new Signal<>("joid");
		BridgeHandler.THREAD.register(thread);
		try {
			SignalTest.fromWorker(() -> signal.set("worker"));
			Assert.assertEquals("joid", signal.peek());
			thread.runTasks();
			Assert.assertEquals("worker", signal.peek());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void appliesTheWritesOfAThreadInOrder() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<>("joid").subscribe(received::add);
		BridgeHandler.THREAD.register(thread);
		try {
			SignalTest.fromWorker(() -> signal.set("a").set("b").reset().set("c"));
			Assert.assertTrue(received.isEmpty());
			thread.runTasks();
			Assert.assertEquals(Arrays.asList("a", "b", "joid", "c"), received);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void carriesItsSilenceWithADeferredWrite() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final List<String> received = new ArrayList<>();
		final Signal<String> signal = new Signal<>("joid").subscribe(received::add);
		BridgeHandler.THREAD.register(thread);
		try {
			SignalTest.fromWorker(() -> signal.silent().set("quiet"));
			signal.set("loud");
			thread.runTasks();
			Assert.assertEquals("quiet", signal.peek());
			signal.set("next");
			Assert.assertEquals(Arrays.asList("loud", "next"), received);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void appliesADeferredIncrementOnTheCurrentValue() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final IntegerSignal signal = new IntegerSignal(0);
		BridgeHandler.THREAD.register(thread);
		try {
			SignalTest.fromWorker(() -> signal.increment().increment());
			signal.set(10);
			thread.runTasks();
			Assert.assertEquals(12, signal.peek().intValue());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void groupsTheDeferredWritesOfABatch() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final IntegerSignal left = new IntegerSignal(0);
		final IntegerSignal right = new IntegerSignal(0);
		final List<Integer> received = new ArrayList<>();
		Signal.from(() -> left.get() + right.get()).subscribe(received::add);
		BridgeHandler.THREAD.register(thread);
		try {
			SignalTest.fromWorker(() -> Signal.batch(() -> {
				left.set(1);
				right.set(2);
			}));
			Assert.assertTrue(received.isEmpty());
			thread.runTasks();
			Assert.assertEquals(Collections.singletonList(3), received);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	private static void fromWorker(final Runnable task) throws InterruptedException {
		final Thread worker = ThreadUtils.daemonThread(task, "worker");
		worker.start();
		worker.join();
	}

}