package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LongSignalTest {

	@Test
	public void startsAtZero() {
		final LongSignal signal = new LongSignal();
		Assert.assertEquals(0L, signal.get().longValue());
	}

	@Test
	public void startsWithTheGivenValue() {
		final LongSignal signal = new LongSignal(5L);
		Assert.assertEquals(5L, signal.get().longValue());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals(5L, new LongSignal(5L).set(8L).reset().get().longValue());
	}

	@Test
	public void computesFromItsDefault() {
		final LongSignal signal = new LongSignal(5L);
		signal.increment();
		Assert.assertEquals(6L, signal.get().longValue());
	}

	@Test
	public void incrementsAndDecrements() {
		final LongSignal signal = new LongSignal(1L);
		signal.increment();
		Assert.assertEquals(2L, signal.get().longValue());
		signal.decrement();
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-1L, signal.get().longValue());
	}

	@Test
	public void addsAndSubtracts() {
		final LongSignal signal = new LongSignal(1L);
		signal.add(10000000000L);
		Assert.assertEquals(10000000001L, signal.get().longValue());
		signal.subtract(4L);
		Assert.assertEquals(9999999997L, signal.get().longValue());
	}

	@Test
	public void multipliesAndDividesWholeNumbers() {
		final LongSignal signal = new LongSignal(7L);
		signal.multiply(3L);
		Assert.assertEquals(21L, signal.get().longValue());
		signal.divide(4L);
		Assert.assertEquals(5L, signal.get().longValue());
		signal.divide(-2L);
		Assert.assertEquals(-2L, signal.get().longValue());
	}

	@Test
	public void refusesADivisionByZero() {
		final LongSignal signal = new LongSignal(7L);
		try {
			signal.divide(0L);
			Assert.fail("A division by zero must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The LongSignal of value 7 cannot be divided by 0, check the divisor before calling divide(...)", expected.getMessage());
		}
		Assert.assertEquals(7L, signal.get().longValue());
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Long> received = new ArrayList<>();
		final LongSignal signal = new LongSignal();
		signal.onChange(received::add);
		signal.increment();
		signal.add(2L);
		signal.multiply(1L);
		Assert.assertEquals(Arrays.asList(1L, 3L), received);
	}

	@Test
	public void chainsItsOperations() {
		final LongSignal signal = new LongSignal(1L);
		Assert.assertSame(signal, signal.increment().add(2L).multiply(3L).subtract(2L).divide(2L).decrement());
		Assert.assertEquals(4L, signal.get().longValue());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("LongSignal{5}", new LongSignal(5L).toString());
		Assert.assertEquals("LongSignal{0}", new LongSignal().toString());
	}

}