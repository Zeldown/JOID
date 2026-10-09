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
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(0L, signal.get().longValue());
	}

	@Test
	public void fallsBackOnTheGivenDefault() {
		final LongSignal signal = new LongSignal(5L);
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(5L, signal.get().longValue());
	}

	@Test
	public void startsWithTheGivenValue() {
		final LongSignal signal = LongSignal.of(5L);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(5L, signal.get().longValue());
	}

	@Test
	public void computesFromItsDefault() {
		final LongSignal signal = new LongSignal(5L);
		signal.increment();
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(6L, signal.get().longValue());
	}

	@Test
	public void incrementsAndDecrements() {
		final LongSignal signal = LongSignal.of(1L);
		signal.increment();
		Assert.assertEquals(2L, signal.get().longValue());
		signal.decrement();
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-1L, signal.get().longValue());
	}

	@Test
	public void addsAndSubtracts() {
		final LongSignal signal = LongSignal.of(1L);
		signal.add(10000000000L);
		Assert.assertEquals(10000000001L, signal.get().longValue());
		signal.subtract(4L);
		Assert.assertEquals(9999999997L, signal.get().longValue());
	}

	@Test
	public void multipliesAndDividesWholeNumbers() {
		final LongSignal signal = LongSignal.of(7L);
		signal.multiply(3L);
		Assert.assertEquals(21L, signal.get().longValue());
		signal.divide(4L);
		Assert.assertEquals(5L, signal.get().longValue());
		signal.divide(-2L);
		Assert.assertEquals(-2L, signal.get().longValue());
	}

	@Test(expected = ArithmeticException.class)
	public void refusesADivisionByZero() {
		LongSignal.of(1L).divide(0L);
	}

	@Test
	public void raisesToAPower() {
		final LongSignal signal = LongSignal.of(2L);
		signal.power(40);
		Assert.assertEquals(1099511627776L, signal.get().longValue());
		signal.power(0);
		Assert.assertEquals(1L, signal.get().longValue());
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Long> received = new ArrayList<>();
		final LongSignal signal = new LongSignal();
		signal.subscribe(received::add);
		signal.increment();
		signal.add(2L);
		signal.power(1);
		signal.multiply(1L);
		Assert.assertEquals(Arrays.asList(1L, 3L), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("LongSignal{5}", LongSignal.of(5L).toString());
		Assert.assertEquals("LongSignal{0}", new LongSignal().toString());
	}

	@Test
	public void raisesToAPowerBeyondTheDoublePrecision() {
		final LongSignal signal = LongSignal.of(3L);
		signal.power(39);
		Assert.assertEquals(4052555153018976267L, signal.get().longValue());
	}

	@Test
	public void truncatesANegativePowerTowardZero() {
		final LongSignal half = LongSignal.of(2L);
		half.power(-1);
		Assert.assertEquals(0L, half.get().longValue());
		final LongSignal unit = LongSignal.of(-1L);
		unit.power(-3);
		Assert.assertEquals(-1L, unit.get().longValue());
	}

}