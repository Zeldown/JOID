package dev.joid.lib.utils.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class SignalContextTest {

	@Before
	public void forgetTheEarlierReads() {
		SignalContext.current().takeReads();
	}

	@Test
	public void recordsTheReadsInTheirOrder() {
		final Signal<String> first = Signal.of("a");
		final Signal<String> second = Signal.of("b");
		first.get();
		second.getOrDefault();
		first.isPresent();
		Assert.assertEquals(Arrays.asList(first, second, first), SignalContext.current().takeReads());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void keepsOnlyTheLatestReads() {
		final List<Signal<Integer>> signalList = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			final Signal<Integer> signal = Signal.of(index);
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
		Signal.of("a").peek();
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void leavesTheReadsOfAComputationToIt() {
		final IntegerSignal count = IntegerSignal.of(1);
		final ComputedSignal<Integer> doubled = count.map(value -> value * 2);
		doubled.get();
		Assert.assertEquals(Collections.singletonList(doubled), SignalContext.current().takeReads());
	}

	@Test
	public void putsBackReadsBeforeTheRecentOnes() {
		final Signal<String> first = Signal.of("a");
		final Signal<String> second = Signal.of("b");
		final Signal<String> third = Signal.of("c");
		first.get();
		second.get();
		final List<Signal<?>> readList = SignalContext.current().takeReads();
		third.get();
		SignalContext.current().putBackReads(readList);
		Assert.assertEquals(Arrays.asList(first, second, third), SignalContext.current().takeReads());
	}

	@Test
	public void keepsTheReadsOfEachThreadApart() throws InterruptedException {
		final Signal<String> mine = Signal.of("a");
		final Signal<String> other = Signal.of("b");
		final List<List<Signal<?>>> otherReads = new ArrayList<>();
		mine.get();
		final Thread thread = new Thread(() -> {
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
		Signal.of("a").get();
		Assert.assertTrue(SignalContext.current().hasReads());
		SignalContext.current().clearReads();
		Assert.assertFalse(SignalContext.current().hasReads());
		Assert.assertTrue(SignalContext.current().takeReads().isEmpty());
	}

	@Test
	public void countsEveryReadEvenInsideAComputation() {
		final IntegerSignal count = IntegerSignal.of(1);
		final long total = SignalContext.current().getReadTotal();
		count.map(value -> value * 2).get();
		Assert.assertEquals(total + 2, SignalContext.current().getReadTotal());
	}

	@Test
	public void movesTheEpochOnEveryChange() {
		final long epoch = SignalContext.getEpoch();
		Signal.of("a").set("b").publish();
		Assert.assertTrue(SignalContext.getEpoch() >= epoch + 3);
	}

}