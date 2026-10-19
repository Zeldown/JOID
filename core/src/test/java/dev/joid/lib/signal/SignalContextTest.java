package dev.joid.lib.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.signal.impl.primitive.IntegerSignal;

public class SignalContextTest {

	@Before
	public void forgetTheEarlierReads() {
		SignalContext.current().takeReads();
		SignalContext.current().tracing(true);
	}

	@After
	public void stopTracing() {
		SignalContext.current().tracing(false);
	}

	@Test
	public void tracesNothingByDefault() throws InterruptedException {
		final Signal<String> signal = new Signal<>("a");
		final List<List<Signal<?>>> readList = new ArrayList<>();
		final Thread thread = new Thread(() -> {
			signal.get();
			readList.add(SignalContext.current().takeReads());
		});
		thread.start();
		thread.join();
		Assert.assertEquals(Collections.singletonList(Collections.emptyList()), readList);
	}

	@Test
	public void recordsTheReadsInTheirOrder() {
		final Signal<String> first = new Signal<>("a");
		final Signal<String> second = new Signal<>("b");
		first.get();
		second.get();
		first.isPresent();
		Assert.assertEquals(Arrays.asList(first, second, first), SignalContext.current().takeReads());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void keepsOnlyTheLatestReads() {
		final List<Signal<Integer>> signalList = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			final Signal<Integer> signal = new Signal<>(index);
			signalList.add(signal);
			signal.get();
		}
		final List<Signal<?>> readList = SignalContext.current().takeReads();
		Assert.assertEquals(64, readList.size());
		for (int index = 0; index < 64; index++) {
			Assert.assertSame(signalList.get(36 + index), readList.get(index));
		}
	}

	@Test
	public void ignoresAPeek() {
		new Signal<>("a").peek();
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void ignoresTheReadsWhileTracingIsOff() {
		final Signal<String> signal = new Signal<>("a");
		final boolean previous = SignalContext.current().tracing(false);
		try {
			signal.get();
		} finally {
			SignalContext.current().tracing(previous);
		}
		Assert.assertTrue(previous);
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
		signal.get();
		Assert.assertEquals(Collections.singletonList(signal), SignalContext.current().takeReads());
	}

	@Test
	public void readsUntrackedWithoutFollowingNorTracing() {
		final IntegerSignal count = new IntegerSignal(1);
		final IntegerSignal bonus = new IntegerSignal(10);
		final ComputedSignal<Integer> total = Signal.from(() -> count.get() + SignalContext.current().untracked(bonus::get));
		Assert.assertEquals(11, total.get().intValue());
		bonus.set(20);
		Assert.assertEquals(11, total.get().intValue());
		count.set(2);
		Assert.assertEquals(22, total.get().intValue());
		SignalContext.current().takeReads();
		Assert.assertEquals(20, SignalContext.current().untracked(bonus::get).intValue());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void leavesTheReadsOfAComputationToIt() {
		final IntegerSignal count = new IntegerSignal(1);
		final ComputedSignal<Integer> doubled = count.map(value -> value * 2);
		doubled.get();
		Assert.assertEquals(Collections.singletonList(doubled), SignalContext.current().takeReads());
	}

	@Test
	public void putsBackReadsBeforeTheRecentOnes() {
		final Signal<String> first = new Signal<>("a");
		final Signal<String> second = new Signal<>("b");
		final Signal<String> third = new Signal<>("c");
		first.get();
		second.get();
		final List<Signal<?>> readList = SignalContext.current().takeReads();
		third.get();
		SignalContext.current().putBackReads(readList);
		Assert.assertEquals(Arrays.asList(first, second, third), SignalContext.current().takeReads());
	}

	@Test
	public void keepsTheReadsOfEachThreadApart() throws InterruptedException {
		final Signal<String> mine = new Signal<>("a");
		final Signal<String> other = new Signal<>("b");
		final List<List<Signal<?>>> otherReads = new ArrayList<>();
		mine.get();
		final Thread thread = new Thread(() -> {
			SignalContext.current().tracing(true);
			other.get();
			otherReads.add(SignalContext.current().takeReads());
		});
		thread.start();
		thread.join();
		Assert.assertEquals(Collections.singletonList(Collections.singletonList(other)), otherReads);
		Assert.assertEquals(Collections.singletonList(mine), SignalContext.current().takeReads());
	}

	@Test
	public void clearsTheReadsWithoutTakingThem() {
		new Signal<>("a").get();
		Assert.assertTrue(SignalContext.current().hasReads());
		SignalContext.current().clearReads();
		Assert.assertFalse(SignalContext.current().hasReads());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void countsEveryReadEvenInsideAComputation() {
		final IntegerSignal count = new IntegerSignal(1);
		final long total = SignalContext.current().getReadTotal();
		count.map(value -> value * 2).get();
		Assert.assertEquals(total + 2, SignalContext.current().getReadTotal());
	}

	@Test
	public void keepsTheNullOriginOfASignalWrittenBackSeveralTimes() {
		final Signal<Integer> trigger = new Signal<>(0);
		final Signal<Integer> signal = new Signal<>(null);
		final List<Object> originList = new ArrayList<>();
		trigger.onChange(value -> {
			SignalContext.current().countWriteBack(signal);
			signal.set(5);
			SignalContext.current().countWriteBack(signal);
			originList.add(SignalContext.current().getWriteBackOrigin(signal));
		});
		trigger.set(1);
		Assert.assertEquals(Collections.singletonList(null), originList);
	}

	@Test
	public void movesTheEpochOnEveryChange() {
		final long epoch = SignalContext.getEpoch();
		new Signal<>("a").set("b").fireChange();
		Assert.assertTrue(SignalContext.getEpoch() >= epoch + 2);
	}

}