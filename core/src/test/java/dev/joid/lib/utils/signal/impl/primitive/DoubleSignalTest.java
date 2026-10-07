package dev.joid.lib.utils.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DoubleSignalTest {

	@Test
	public void startsAtZero() {
		final DoubleSignal signal = new DoubleSignal();
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(0D, signal.get(), 0D);
	}

	@Test
	public void fallsBackOnTheGivenDefault() {
		final DoubleSignal signal = new DoubleSignal(1.5D);
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(1.5D, signal.get(), 0D);
	}

	@Test
	public void startsWithTheGivenValue() {
		final DoubleSignal signal = DoubleSignal.of(1.5D);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(1.5D, signal.get(), 0D);
	}

	@Test
	public void computesFromItsDefault() {
		final DoubleSignal signal = new DoubleSignal(2D);
		signal.add(0.5D);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(2.5D, signal.get(), 0D);
	}

	@Test
	public void addsAndSubtracts() {
		final DoubleSignal signal = DoubleSignal.of(1D);
		signal.add(2.5D);
		Assert.assertEquals(3.5D, signal.get(), 0D);
		signal.subtract(0.25D);
		Assert.assertEquals(3.25D, signal.get(), 0D);
	}

	@Test
	public void incrementsAndDecrements() {
		final DoubleSignal signal = DoubleSignal.of(0.5D);
		signal.increment();
		Assert.assertEquals(1.5D, signal.get(), 0D);
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-0.5D, signal.get(), 0D);
	}

	@Test
	public void multipliesAndDivides() {
		final DoubleSignal signal = DoubleSignal.of(3D);
		signal.multiply(2.5D);
		Assert.assertEquals(7.5D, signal.get(), 0D);
		signal.divide(0.5D);
		Assert.assertEquals(15D, signal.get(), 0D);
	}

	@Test(expected = ArithmeticException.class)
	public void refusesADivisionByZero() {
		DoubleSignal.of(1D).divide(0D);
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Double> received = new ArrayList<>();
		final DoubleSignal signal = new DoubleSignal();
		signal.subscribe(received::add);
		signal.increment();
		signal.multiply(4D);
		signal.multiply(1D);
		signal.add(0D);
		Assert.assertEquals(Arrays.asList(1D, 4D), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("DoubleSignal{1.5}", DoubleSignal.of(1.5D).toString());
		Assert.assertEquals("DoubleSignal{0.0}", new DoubleSignal().toString());
	}

}