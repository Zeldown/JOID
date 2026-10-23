package dev.joid.lib.signal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDWarnings;
import dev.joid.internal.TestUtils;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.iterable.MapSignal;
import dev.joid.lib.signal.impl.iterable.SetSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;

public class ComputedSignalTest {

	@After
	public void forgetTheWarnings() {
		JOIDWarnings.forget();
	}

	@Test
	public void computesItsValueFromTheSignalsItReads() {
		final Signal<String> name = new Signal<>("joid");
		final IntegerSignal count = new IntegerSignal(2);
		final ComputedSignal<String> text = Signal.from(() -> name.get() + " x" + count.get());
		Assert.assertEquals("joid x2", text.get());
		name.set("ui");
		count.increment();
		Assert.assertEquals("ui x3", text.get());
	}

	@Test
	public void waitsForAReadBeforeComputing() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal count = new IntegerSignal(1);
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
		final IntegerSignal count = new IntegerSignal(1);
		final Signal<String> other = new Signal<>("joid");
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
		final BooleanSignal left = new BooleanSignal(true);
		final Signal<String> first = new Signal<>("a");
		final Signal<String> second = new Signal<>("b");
		final ComputedSignal<String> chosen = Signal.from(() -> {
			evaluations.incrementAndGet();
			return left.get() ? first.get() : second.get();
		}).onChange(value -> {});
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
		final IntegerSignal source = new IntegerSignal(1);
		final ComputedSignal<Integer> doubled = source.map(value -> value * 2);
		final ComputedSignal<Integer> tripled = source.map(value -> value * 3);
		final ComputedSignal<String> sum = Signal.from(() -> {
			final String value = doubled.get() + "+" + tripled.get();
			computed.add(value);
			return value;
		}).onChange(received::add);
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
		final IntegerSignal source = new IntegerSignal(1);
		final ComputedSignal<Boolean> odd = source.map(value -> value % 2 == 1);
		final ComputedSignal<String> text = odd.map(value -> {
			evaluations.incrementAndGet();
			return value ? "odd" : "even";
		}).onChange(received::add);
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
		final IntegerSignal count = new IntegerSignal(1);
		count.map(value -> value * 10).onChange(received::add);
		count.increment();
		count.set(2);
		count.set(3);
		Assert.assertEquals(Arrays.asList(20, 30), received);
	}

	@Test
	public void stopsNotifyingOnceUnsubscribed() {
		final List<Integer> received = new ArrayList<>();
		final SignalListener<Integer> subscriber = received::add;
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<Integer> tenfold = count.map(value -> value * 10).onChange(subscriber);
		count.increment();
		tenfold.removeOnChange(subscriber);
		count.increment();
		Assert.assertEquals(Collections.singletonList(20), received);
		Assert.assertFalse(count.isObserved());
		Assert.assertEquals(30, tenfold.get().intValue());
	}

	@Test
	public void stopsFollowingOnceItsListenerRemovesItself() {
		final AtomicInteger calls = new AtomicInteger();
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<Integer> tenfold = count.map(value -> value * 10);
		tenfold.onChange(new SignalListener<Integer>() {

			@Override
			public void apply(final Integer value) {
				calls.incrementAndGet();
				tenfold.removeOnChange(this);
			}

		});
		count.increment();
		count.increment();
		Assert.assertEquals(1, calls.get());
		Assert.assertFalse(tenfold.hasListeners());
		Assert.assertFalse(count.isObserved());
	}

	@Test
	public void notifiesOnceForABatch() {
		final List<String> received = new ArrayList<>();
		final Signal<String> first = new Signal<>("a");
		final Signal<String> second = new Signal<>("b");
		Signal.from(() -> first.get() + second.get()).onChange(received::add);
		Signal.batch(() -> {
			first.set("c");
			second.set("d");
			first.set("e");
		});
		Assert.assertEquals(Collections.singletonList("ed"), received);
	}

	@Test
	public void readsTheLatestValueInsideABatch() {
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<Integer> doubled = count.map(value -> value * 2).onChange(value -> {});
		Signal.batch(() -> {
			count.set(2);
			Assert.assertEquals(4, doubled.get().intValue());
			count.set(3);
			Assert.assertEquals(6, doubled.get().intValue());
		});
	}

	@Test
	public void chainsMaps() {
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<String> text = count.map(value -> value + 1).map(value -> value * 10).map(value -> "Total: " + value);
		Assert.assertEquals("Total: 20", text.get());
		count.set(4);
		Assert.assertEquals("Total: 50", text.get());
	}

	@Test
	public void combinesComputedSignals() {
		final IntegerSignal price = new IntegerSignal(3);
		final IntegerSignal quantity = new IntegerSignal(2);
		final ComputedSignal<Integer> total = Signal.from(() -> price.get() * quantity.get());
		final ComputedSignal<String> text = Signal.from(() -> "Total: " + total.get());
		quantity.increment();
		Assert.assertEquals("Total: 9", text.get());
	}

	@Test
	public void followsTheMutationsOfAListSignal() {
		final List<Integer> received = new ArrayList<>();
		final ListSignal<String> list = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		final ComputedSignal<Integer> size = Signal.from(list::size).onChange(received::add);
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
		final MapSignal<String, Integer> map = new MapSignal<>(new HashMap<>());
		final ComputedSignal<Integer> total = Signal.from(() -> map.values().stream().mapToInt(Integer::intValue).sum());
		map.put("a", 1);
		map.put("b", 2);
		Assert.assertEquals(3, total.get().intValue());
		map.remove("a");
		Assert.assertEquals(2, total.get().intValue());
	}

	@Test
	public void followsTheMutationsOfASetSignal() {
		final SetSignal<String> set = new SetSignal<>(new HashSet<>());
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
		final StringSignal name = new StringSignal("joid");
		name.map(String::length).map(length -> name.peek() + ":" + length).onChange(received::add);
		name.append("-ui");
		name.toUpperCase();
		name.substring(0, 2);
		Assert.assertEquals(Arrays.asList("joid-ui:7", "JO:2"), received);
	}

	@Test
	public void followsAManualPublish() {
		final List<StringBuilder> received = new ArrayList<>();
		final Signal<StringBuilder> builder = new Signal<>(new StringBuilder("a"));
		final ComputedSignal<String> text = builder.map(StringBuilder::toString);
		builder.onChange(received::add);
		Assert.assertEquals("a", text.get());
		builder.get().append("b");
		builder.fireChange();
		Assert.assertEquals("ab", text.get());
		Assert.assertEquals(1, received.size());
	}

	@Test
	public void staysConsistentAfterASilentChange() {
		final List<String> received = new ArrayList<>();
		final Signal<String> name = new Signal<>("joid").onChange(received::add);
		final ComputedSignal<String> upper = name.map(String::toUpperCase);
		upper.onChange(received::add);
		Assert.assertEquals("JOID", upper.get());
		name.silent(signal -> signal.set("ui"));
		Assert.assertEquals("UI", upper.get());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void notifiesASignalDerivedTwiceAfterASilentChange() {
		final List<String> received = new ArrayList<>();
		final Signal<String> name = new Signal<>("a");
		final ComputedSignal<String> label = name.map(String::toUpperCase).map(value -> value + "!");
		label.onChange(received::add);
		name.silent(signal -> signal.set("b"));
		name.set("c");
		Assert.assertEquals(Collections.singletonList("C!"), received);
	}

	@Test
	public void followsADependencyItSwitchedToDuringASilentChange() {
		final List<Integer> received = new ArrayList<>();
		final BooleanSignal first = new BooleanSignal(true);
		final IntegerSignal a = new IntegerSignal(1);
		final IntegerSignal b = new IntegerSignal(2);
		final ComputedSignal<Integer> shown = Signal.from(() -> first.get() ? a.get() : b.get());
		shown.onChange(received::add);
		first.silent(signal -> signal.set(false));
		Assert.assertTrue(received.isEmpty());
		b.set(5);
		b.set(6);
		Assert.assertEquals(Arrays.asList(5, 6), received);
	}

	@Test
	public void notifiesAChangeMadeOnAnotherThread() throws InterruptedException {
		final List<String> received = Collections.synchronizedList(new ArrayList<>());
		final Signal<String> name = new Signal<>("joid");
		name.map(String::toUpperCase).onChange(received::add);
		final Thread thread = new Thread(() -> name.set("ui"));
		thread.start();
		thread.join();
		Assert.assertEquals(Collections.singletonList("UI"), received);
	}

	@Test
	public void followsOnlyTheReadsOfItsOwnThread() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal mine = new IntegerSignal(1);
		final IntegerSignal other = new IntegerSignal(1);
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
		final IntegerSignal count = new IntegerSignal(1);
		final Supplier<String> supplier = count.map(value -> "Clicks: " + value);
		count.increment();
		Assert.assertEquals("Clicks: 2", supplier.get());
	}

	@Test
	public void refusesToBeWrittenLikeAPlainSignal() {
		final Signal<Integer> source = new Signal<>(2);
		final ComputedSignal<Integer> doubled = source.map(value -> value * 2);
		Assert.assertFalse(doubled.isWritable());
		Assert.assertTrue(source.isWritable());
		try {
			doubled.set(5);
			Assert.fail("A computed signal cannot be set");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ComputedSignal is computed from other signals and cannot be set to 5, set the signals it reads instead", expected.getMessage());
		}
		try {
			doubled.reset();
			Assert.fail("A computed signal cannot be reset");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ComputedSignal is computed from other signals and cannot be reset, set the signals it reads instead", expected.getMessage());
		}
		Assert.assertEquals(4, doubled.peek().intValue());
	}

	@Test
	public void refusesToBeWrittenWithoutComputingItsValue() {
		final Supplier<Integer> failing = () -> {
			throw new IllegalArgumentException("The supplier failed");
		};
		try {
			Signal.from(failing).set(5);
			Assert.fail("A computed signal cannot be set");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ComputedSignal is computed from other signals and cannot be set to 5, set the signals it reads instead", expected.getMessage());
		}
	}

	@Test
	public void warnsOnceInDevModeWhenItReadsNoSignal() {
		final boolean devMode = JOIDEnv.isDevMode();
		final IntegerSignal count = new IntegerSignal(1);
		JOIDWarnings.forget();
		try {
			JOIDEnv.devMode(true);
			final ComputedSignal<Integer> fixed = Signal.from(() -> 42);
			final String output = TestUtils.captureErr(() -> {
				Assert.assertEquals(42, fixed.get().intValue());
				Assert.assertEquals(42, Signal.from(() -> 42).get().intValue());
			});
			Assert.assertTrue(output, output.startsWith("[JOID] A Signal.from(...) of ComputedSignalTest reads no signal, so its value never changes: read a signal with get() inside it, or pass a lambda to the setter to read the value at every frame" + System.lineSeparator()));
			Assert.assertEquals("", TestUtils.captureErr(() -> count.map(value -> value + 1).get()));
		} finally {
			JOIDEnv.devMode(devMode);
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
			Assert.assertEquals("A ComputedSignal cannot read itself while it computes its value, remove the read of this signal from its function", exception.getMessage());
		}
	}

	@Test
	public void computesAgainAfterAFailure() {
		final IntegerSignal count = new IntegerSignal(0);
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
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<Integer> inverse = count.map(value -> 10 / value).onChange(received::add);
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
	public void notifiesASignalDerivedFromAFailedSignalAgain() {
		final List<String> received = new ArrayList<>();
		final IntegerSignal count = new IntegerSignal(1);
		count.map(value -> 10 / value).map(value -> "x" + value).onChange(received::add);
		try {
			count.set(0);
			Assert.fail();
		} catch (final ArithmeticException exception) {
			count.set(5);
		}
		Assert.assertEquals(Collections.singletonList("x2"), received);
	}

	@Test
	public void startsEmptyForANullValue() {
		final Signal<String> name = new Signal<>();
		final ComputedSignal<String> upper = name.map(value -> value == null ? null : value.toUpperCase());
		Assert.assertFalse(upper.isPresent());
		Assert.assertNull(upper.get());
		name.set("joid");
		Assert.assertTrue(upper.isPresent());
		Assert.assertEquals("JOID", upper.peek());
	}

	@Test
	public void keepsAPlainValueAsAConstant() {
		final ComputedSignal<String> name = Signal.from("joid");
		SignalContext.current().takeReads();
		Assert.assertTrue(name.isConstant());
		Assert.assertEquals("joid", name.get());
		Assert.assertEquals("joid", name.peek());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
		Assert.assertFalse(Signal.from(() -> "joid").isConstant());
		Assert.assertFalse(new IntegerSignal(1).map(value -> value * 2).isConstant());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("ComputedSignal{2}", new IntegerSignal(1).map(value -> value * 2).toString());
		JOIDWarnings.forget();
		final String output = TestUtils.captureErr(() -> Assert.assertEquals("ComputedSignal{null}", Signal.from(() -> null).toString()));
		Assert.assertTrue(output, output.startsWith("[JOID] A Signal.from(...) of ComputedSignalTest reads no signal"));
	}

	@Test
	public void releasesAComputedSignalNoLongerReferenced() {
		final IntegerSignal count = new IntegerSignal(1);
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
	public void computesEachOfItsMapsOnceAChange() {
		final AtomicInteger evaluations = new AtomicInteger();
		final IntegerSignal count = new IntegerSignal(0);
		final ComputedSignal<String> color = count.map(value -> {
			evaluations.incrementAndGet();
			return value >= 3 ? "green" : "gray";
		});
		final ComputedSignal<String> text = count.map(value -> {
			evaluations.incrementAndGet();
			return "Clicks: " + value;
		});
		final ComputedSignal<String> message = count.map(value -> {
			evaluations.incrementAndGet();
			return value == 0 ? "Nobody clicked yet" : "Thanks!";
		});
		for (int index = 0; index < 1000; index++) {
			count.increment();
			color.get();
			text.get();
			message.get();
			color.get();
		}
		Assert.assertEquals(3000, evaluations.get());
	}

}