package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IntegerSignalTest {

	@Test
	public void startsAtZero() {
		final IntegerSignal signal = new IntegerSignal();
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(0, signal.get().intValue());
	}

	@Test
	public void fallsBackOnTheGivenDefault() {
		final IntegerSignal signal = new IntegerSignal(5);
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(5, signal.get().intValue());
	}

	@Test
	public void startsWithTheGivenValue() {
		final IntegerSignal signal = IntegerSignal.of(5);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(5, signal.get().intValue());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals(5, IntegerSignal.of(5).set(8).reset().get().intValue());
	}

	@Test
	public void computesFromItsDefault() {
		final IntegerSignal signal = new IntegerSignal(5);
		signal.increment();
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(6, signal.get().intValue());
	}

	@Test
	public void incrementsAndDecrements() {
		final IntegerSignal signal = IntegerSignal.of(1);
		signal.increment();
		Assert.assertEquals(2, signal.get().intValue());
		signal.decrement();
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-1, signal.get().intValue());
	}

	@Test
	public void addsAndSubtracts() {
		final IntegerSignal signal = IntegerSignal.of(1);
		signal.add(10);
		Assert.assertEquals(11, signal.get().intValue());
		signal.subtract(4);
		Assert.assertEquals(7, signal.get().intValue());
	}

	@Test
	public void multipliesAndDividesWholeNumbers() {
		final IntegerSignal signal = IntegerSignal.of(7);
		signal.multiply(3);
		Assert.assertEquals(21, signal.get().intValue());
		signal.divide(4);
		Assert.assertEquals(5, signal.get().intValue());
		signal.divide(-2);
		Assert.assertEquals(-2, signal.get().intValue());
	}

	@Test(expected = ArithmeticException.class)
	public void refusesADivisionByZero() {
		IntegerSignal.of(1).divide(0);
	}

	@Test
	public void raisesToAPower() {
		final IntegerSignal signal = IntegerSignal.of(3);
		signal.power(4);
		Assert.assertEquals(81, signal.get().intValue());
		signal.power(0);
		Assert.assertEquals(1, signal.get().intValue());
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Integer> received = new ArrayList<>();
		final IntegerSignal signal = new IntegerSignal();
		signal.subscribe(received::add);
		signal.increment();
		signal.add(2);
		signal.power(1);
		signal.multiply(1);
		Assert.assertEquals(Arrays.asList(1, 3), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("IntegerSignal{5}", IntegerSignal.of(5).toString());
		Assert.assertEquals("IntegerSignal{0}", new IntegerSignal().toString());
	}

}