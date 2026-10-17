package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IntegerSignalTest {

	@Test
	public void startsAtZero() {
		final IntegerSignal signal = IntegerSignal.create();
		Assert.assertEquals(0, signal.get().intValue());
	}

	@Test
	public void startsWithTheGivenValue() {
		final IntegerSignal signal = IntegerSignal.create(5);
		Assert.assertEquals(5, signal.get().intValue());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals(5, IntegerSignal.create(5).set(8).reset().get().intValue());
	}

	@Test
	public void computesFromItsDefault() {
		final IntegerSignal signal = IntegerSignal.create(5);
		signal.increment();
		Assert.assertEquals(6, signal.get().intValue());
	}

	@Test
	public void incrementsAndDecrements() {
		final IntegerSignal signal = IntegerSignal.create(1);
		signal.increment();
		Assert.assertEquals(2, signal.get().intValue());
		signal.decrement();
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-1, signal.get().intValue());
	}

	@Test
	public void addsAndSubtracts() {
		final IntegerSignal signal = IntegerSignal.create(1);
		signal.add(10);
		Assert.assertEquals(11, signal.get().intValue());
		signal.subtract(4);
		Assert.assertEquals(7, signal.get().intValue());
	}

	@Test
	public void multipliesAndDividesWholeNumbers() {
		final IntegerSignal signal = IntegerSignal.create(7);
		signal.multiply(3);
		Assert.assertEquals(21, signal.get().intValue());
		signal.divide(4);
		Assert.assertEquals(5, signal.get().intValue());
		signal.divide(-2);
		Assert.assertEquals(-2, signal.get().intValue());
	}

	@Test
	public void refusesADivisionByZero() {
		final IntegerSignal signal = IntegerSignal.create(7);
		try {
			signal.divide(0);
			Assert.fail("A division by zero must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The IntegerSignal of value 7 cannot be divided by 0, check the divisor before calling divide(...)", expected.getMessage());
		}
		Assert.assertEquals(7, signal.get().intValue());
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Integer> received = new ArrayList<>();
		final IntegerSignal signal = IntegerSignal.create();
		signal.onChange(received::add);
		signal.increment();
		signal.add(2);
		signal.multiply(1);
		Assert.assertEquals(Arrays.asList(1, 3), received);
	}

	@Test
	public void chainsItsOperations() {
		final IntegerSignal signal = IntegerSignal.create(1);
		Assert.assertSame(signal, signal.increment().add(2).multiply(3).subtract(2).divide(2).decrement());
		Assert.assertEquals(4, signal.get().intValue());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("IntegerSignal{5}", IntegerSignal.create(5).toString());
		Assert.assertEquals("IntegerSignal{0}", IntegerSignal.create().toString());
	}

}