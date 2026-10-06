package dev.joid.lib.utils.signal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.utils.signal.impl.iterable.ListSignal;
import dev.joid.lib.utils.signal.impl.iterable.MapSignal;
import dev.joid.lib.utils.signal.impl.iterable.SetSignal;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;

public class ComputedSignalTest {

	@Test
	public void computesItsValueFromTheSignalsItReads() {
		final Signal<String> name = Signal.of("joid");
		final IntegerSignal count = IntegerSignal.of(2);
		final ComputedSignal<String> text = Signal.from(() -> name.get() + " x" + count.get());
		Assert.assertEquals("joid x2", text.get());
		name.set("ui");
		count.increment();
		Assert.assertEquals("ui x3", text.get());
	}

	@Test
	public void waitsForAReadBeforeComputing() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> doubled = Signal.from(() -> {
			evaluations.incrementAndGet();
			return count.get() * 2;
		});
		Assert.assertEquals(0, evaluations.get());
		count.increment();
		count.increment();
		Assert.assertEquals(0, evaluations.get());
		Assert.assertEquals(6, doubled.get().intValue());
		Assert.assertEquals(1, evaluations.get());
	}

	@Test
	public void keepsItsValueUntilADependencyChanges() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal count = IntegerSignal.of(1);
		final Signal<String> other = Signal.of("joid");
		final ComputedSignal<Integer> doubled = Signal.from(() -> {
			evaluations.incrementAndGet();
			return count.get() * 2;
		});
		doubled.get();
		doubled.get();
		other.set("ui");
		doubled.get();
		Assert.assertEquals(1, evaluations.get());
		count.increment();
		doubled.get();
		doubled.get();
		Assert.assertEquals(2, evaluations.get());
	}

	@Test
	public void followsOnlyTheSignalsOfItsLastComputation() {
		final AtomicInteger evaluations = new AtomicInteger();
		final BooleanSignal left = BooleanSignal.of(true);
		final Signal<String> first = Signal.of("a");
		final Signal<String> second = Signal.of("b");
		final ComputedSignal<String> chosen = Signal.from(() -> {
			evaluations.incrementAndGet();
			return left.get() ? first.get() : second.get();
		}).subscribe(value -> true);
		Assert.assertEquals("a", chosen.get());
		second.set("c");
		Assert.assertEquals("a", chosen.get());
		Assert.assertEquals(1, evaluations.get());
		Assert.assertFalse(second.isObserved());
		left.toggle();
		Assert.assertEquals("c", chosen.get());
		Assert.assertTrue(second.isObserved());
		Assert.assertFalse(first.isObserved());
		first.set("d");
		Assert.assertEquals("c", chosen.get());
		Assert.assertEquals(2, evaluations.get());
	}

	@Test
	public void computesADiamondOnceWithConsistentValues() {
		final List<String> computed = new ArrayList<>();
		final List<String> received = new ArrayList<>();
		final IntegerSignal source = IntegerSignal.of(1);
		final ComputedSignal<Integer> doubled = source.map(value -> value * 2);
		final ComputedSignal<Integer> tripled = source.map(value -> value * 3);
		final ComputedSignal<String> sum = Signal.from(() -> {
			final String value = doubled.get() + "+" + tripled.get();
			computed.add(value);
			return value;
		}).subscribe(received::add);
		source.increment();
		source.set(5);
		Assert.assertEquals("10+15", sum.get());
		Assert.assertEquals(Arrays.asList("2+3", "4+6", "10+15"), computed);
		Assert.assertEquals(Arrays.asList("4+6", "10+15"), received);
	}

	@Test
	public void stopsAtAnEqualValue() {
		final AtomicInteger evaluations = new AtomicInteger();
		final List<String> received = new ArrayList<>();
		final IntegerSignal source = IntegerSignal.of(1);
		final ComputedSignal<Boolean> odd = source.map(value -> value % 2 == 1);
		final ComputedSignal<String> text = odd.map(value -> {
			evaluations.incrementAndGet();
			return value ? "odd" : "even";
		}).subscribe(received::add);
		source.set(3);
		source.set(5);
		Assert.assertEquals("odd", text.get());
		Assert.assertEquals(1, evaluations.get());
		source.set(4);
		Assert.assertEquals(2, evaluations.get());
		Assert.assertEquals(Collections.singletonList("even"), received);
	}

	@Test
	public void notifiesItsSubscribersWhenItsValueChanges() {
		final List<Integer> received = new ArrayList<>();
		final IntegerSignal count = IntegerSignal.of(1);
		count.map(value -> value * 10).subscribe(received::add);
		count.increment();
		count.set(2);
		count.set(3);
		Assert.assertEquals(Arrays.asList(20, 30), received);
	}

	@Test
	public void stopsNotifyingOnceUnsubscribed() {
		final List<Integer> received = new ArrayList<>();
		final SignalSubscriber<Integer> subscriber = received::add;
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> tenfold = count.map(value -> value * 10).subscribe(subscriber);
		count.increment();
		tenfold.unsubscribe(subscriber);
		count.increment();
		Assert.assertEquals(Collections.singletonList(20), received);
		Assert.assertFalse(count.isObserved());
		Assert.assertEquals(30, tenfold.get().intValue());
	}

	@Test
	public void dropsASubscriberReturningFalse() {
		final AtomicInteger calls = new AtomicInteger();
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> tenfold = count.map(value -> value * 10).subscribe(value -> calls.incrementAndGet() < 0);
		count.increment();
		count.increment();
		Assert.assertEquals(1, calls.get());
		Assert.assertTrue(tenfold.getEventSet().isEmpty());
		Assert.assertFalse(count.isObserved());
	}

	@Test
	public void notifiesOnceForABatch() {
		final List<String> received = new ArrayList<>();
		final Signal<String> first = Signal.of("a");
		final Signal<String> second = Signal.of("b");
		Signal.from(() -> first.get() + second.get()).subscribe(received::add);
		Signal.batch(() -> {
			first.set("c");
			second.set("d");
			first.set("e");
		});
		Assert.assertEquals(Collections.singletonList("ed"), received);
	}

	@Test
	public void readsTheLatestValueInsideABatch() {
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> doubled = count.map(value -> value * 2).subscribe(value -> true);
		Signal.batch(() -> {
			count.set(2);
			Assert.assertEquals(4, doubled.get().intValue());
			count.set(3);
			Assert.assertEquals(6, doubled.get().intValue());
		});
	}

	@Test
	public void chainsMaps() {
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<String> text = count.map(value -> value + 1).map(value -> value * 10).map(value -> "Total: " + value);
		Assert.assertEquals("Total: 20", text.get());
		count.set(4);
		Assert.assertEquals("Total: 50", text.get());
	}

	@Test
	public void combinesComputedSignals() {
		final IntegerSignal price = IntegerSignal.of(3);
		final IntegerSignal quantity = IntegerSignal.of(2);
		final ComputedSignal<Integer> total = Signal.from(() -> price.get() * quantity.get());
		final ComputedSignal<String> text = Signal.from(() -> "Total: " + total.get());
		quantity.increment();
		Assert.assertEquals("Total: 9", text.get());
	}

	@Test
	public void followsTheMutationsOfAListSignal() {
		final List<Integer> received = new ArrayList<>();
		final ListSignal<String> list = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		final ComputedSignal<Integer> size = Signal.from(list::size).subscribe(received::add);
		list.add("b");
		list.set(0, "c");
		list.remove("b");
		list.clear();
		Assert.assertEquals(0, size.get().intValue());
		Assert.assertEquals(Arrays.asList(2, 1, 0), received);
		Assert.assertEquals("c", Signal.from(() -> list.isEmpty() ? "c" : list.get(0)).get());
	}

	@Test
	public void followsTheMutationsOfAMapSignal() {
		final MapSignal<String, Integer> map = MapSignal.of(new HashMap<>());
		final ComputedSignal<Integer> total = Signal.from(() -> map.values().stream().mapToInt(Integer::intValue).sum());
		map.put("a", 1);
		map.put("b", 2);
		Assert.assertEquals(3, total.get().intValue());
		map.remove("a");
		Assert.assertEquals(2, total.get().intValue());
	}

	@Test
	public void followsTheMutationsOfASetSignal() {
		final SetSignal<String> set = SetSignal.of(new HashSet<>());
		final ComputedSignal<Boolean> containsJoid = Signal.from(() -> set.contains("joid"));
		Assert.assertFalse(containsJoid.get());
		set.add("joid");
		Assert.assertTrue(containsJoid.get());
		set.remove("joid");
		Assert.assertFalse(containsJoid.get());
	}

	@Test
	public void followsTheOperationsOfAPrimitiveSignal() {
		final List<String> received = new ArrayList<>();
		final StringSignal name = StringSignal.of("joid");
		name.map(String::length).map(length -> name.peek() + ":" + length).subscribe(received::add);
		name.append("-ui");
		name.toUpperCase();
		name.substring(0, 2);
		Assert.assertEquals(Arrays.asList("joid-ui:7", "JO:2"), received);
	}

	@Test
	public void followsAManualPublish() {
		final List<StringBuilder> received = new ArrayList<>();
		final Signal<StringBuilder> builder = Signal.of(new StringBuilder("a"));
		final ComputedSignal<String> text = builder.map(StringBuilder::toString);
		builder.subscribe(received::add);
		Assert.assertEquals("a", text.get());
		builder.get().append("b");
		builder.publish();
		Assert.assertEquals("ab", text.get());
		Assert.assertEquals(1, received.size());
	}

	@Test
	public void staysConsistentAfterASilentChange() {
		final List<String> received = new ArrayList<>();
		final Signal<String> name = Signal.of("joid").subscribe(received::add);
		final ComputedSignal<String> upper = name.map(String::toUpperCase);
		Assert.assertEquals("JOID", upper.get());
		name.silent().set("ui");
		Assert.assertEquals("UI", upper.get());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void notifiesAChangeMadeOnAnotherThread() throws InterruptedException {
		final List<String> received = Collections.synchronizedList(new ArrayList<>());
		final Signal<String> name = new Signal<>("joid");
		name.map(String::toUpperCase).subscribe(received::add);
		final Thread thread = new Thread(() -> name.set("ui"));
		thread.start();
		thread.join();
		Assert.assertEquals(Collections.singletonList("UI"), received);
	}

	@Test
	public void followsOnlyTheReadsOfItsOwnThread() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal mine = IntegerSignal.of(1);
		final IntegerSignal other = IntegerSignal.of(1);
		final ComputedSignal<Integer> doubled = Signal.from(() -> {
			evaluations.incrementAndGet();
			final Thread thread = new Thread(other::get);
			thread.start();
			try {
				thread.join();
			} catch (final InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
			return mine.get() * 2;
		});
		Assert.assertEquals(2, doubled.get().intValue());
		other.increment();
		Assert.assertEquals(2, doubled.get().intValue());
		Assert.assertEquals(1, evaluations.get());
	}

	@Test
	public void isASupplier() {
		final IntegerSignal count = IntegerSignal.of(1);
		final Supplier<String> supplier = count.map(value -> "Clicks: " + value);
		count.increment();
		Assert.assertEquals("Clicks: 2", supplier.get());
	}

	@Test
	public void refusesToBeSet() {
		final ComputedSignal<Integer> computed = IntegerSignal.of(1).map(value -> value * 2);
		try {
			computed.set(3);
			Assert.fail();
		} catch (final UnsupportedOperationException exception) {
			Assert.assertEquals("A ComputedSignal is read-only and takes the value of its function: set the signals it reads instead", exception.getMessage());
		}
		Assert.assertEquals(2, computed.get().intValue());
	}

	@Test
	public void refusesToBeReset() {
		final ComputedSignal<Integer> computed = IntegerSignal.of(1).map(value -> value * 2);
		try {
			computed.reset();
			Assert.fail();
		} catch (final UnsupportedOperationException exception) {
			Assert.assertEquals("A ComputedSignal is read-only and has no default value to reset to: reset the signals it reads instead", exception.getMessage());
		}
	}

	@Test
	public void refusesToReadItself() {
		final List<ComputedSignal<Integer>> self = new ArrayList<>();
		self.add(Signal.from(() -> self.get(0).get() + 1));
		try {
			self.get(0).get();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("A ComputedSignal cannot read itself while it computes its value", exception.getMessage());
		}
	}

	@Test
	public void computesAgainAfterAFailure() {
		final IntegerSignal count = IntegerSignal.of(0);
		final ComputedSignal<Integer> inverse = count.map(value -> 10 / value);
		try {
			inverse.get();
			Assert.fail();
		} catch (final ArithmeticException exception) {
			count.set(5);
		}
		Assert.assertEquals(2, inverse.get().intValue());
	}

	@Test
	public void notifiesAgainAfterAFailure() {
		final List<Integer> received = new ArrayList<>();
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> inverse = count.map(value -> 10 / value).subscribe(received::add);
		try {
			count.set(0);
			Assert.fail();
		} catch (final ArithmeticException exception) {
			count.set(5);
		}
		Assert.assertEquals(Collections.singletonList(2), received);
		Assert.assertEquals(2, inverse.get().intValue());
	}

	@Test
	public void startsEmptyForANullValue() {
		final Signal<String> name = new Signal<>();
		final ComputedSignal<String> upper = name.map(value -> value == null ? null : value.toUpperCase());
		Assert.assertFalse(upper.isPresent());
		Assert.assertNull(upper.getOrDefault());
		name.set("joid");
		Assert.assertTrue(upper.isPresent());
		Assert.assertEquals("JOID", upper.peek());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("ComputedSignal{2}", IntegerSignal.of(1).map(value -> value * 2).toString());
		Assert.assertEquals("ComputedSignal{null}", Signal.from(() -> null).toString());
	}

	@Test
	public void releasesAComputedSignalNoLongerReferenced() {
		final IntegerSignal count = IntegerSignal.of(1);
		final WeakReference<ComputedSignal<Integer>> reference = new WeakReference<>(count.map(value -> value * 2));
		Assert.assertEquals(2, reference.get().get().intValue());
		SignalContext.current().takeReads();
		Assert.assertFalse(count.isObserved());
		for (int attempt = 0; attempt < 50 && reference.get() != null; attempt++) {
			System.gc();
		}
		Assert.assertNull(reference.get());
	}

	@Test
	public void staysFastOnAChange() {
		final IntegerSignal count = IntegerSignal.of(0);
		final ComputedSignal<String> color = count.map(value -> value >= 3 ? "green" : "gray");
		final ComputedSignal<String> text = count.map(value -> "Clicks: " + value);
		final ComputedSignal<String> message = count.map(value -> value == 0 ? "Nobody clicked yet" : "Thanks!");
		final int changes = 200000;
		final long start = System.nanoTime();
		for (int index = 0; index < changes; index++) {
			count.increment();
			color.get();
			text.get();
			message.get();
		}
		Assert.assertTrue((System.nanoTime() - start) / changes < 5000L);
	}

}