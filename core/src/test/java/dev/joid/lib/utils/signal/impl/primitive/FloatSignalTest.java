package dev.joid.lib.utils.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FloatSignalTest {

	@Test
	public void startsAtZero() {
		final FloatSignal signal = new FloatSignal();
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(0F, signal.getOrDefault(), 0F);
	}

	@Test
	public void fallsBackOnTheGivenDefault() {
		final FloatSignal signal = new FloatSignal(1.5F);
		Assert.assertFalse(signal.isPresent());
		Assert.assertEquals(1.5F, signal.getOrDefault(), 0F);
	}

	@Test
	public void startsWithTheGivenValue() {
		final FloatSignal signal = FloatSignal.of(1.5F);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(1.5F, signal.getOrDefault(), 0F);
	}

	@Test
	public void computesFromItsDefault() {
		final FloatSignal signal = new FloatSignal(2F);
		signal.add(0.5F);
		Assert.assertTrue(signal.isPresent());
		Assert.assertEquals(2.5F, signal.getOrDefault(), 0F);
	}

	@Test
	public void addsAndSubtracts() {
		final FloatSignal signal = FloatSignal.of(1F);
		signal.add(2.5F);
		Assert.assertEquals(3.5F, signal.getOrDefault(), 0F);
		signal.subtract(0.25F);
		Assert.assertEquals(3.25F, signal.getOrDefault(), 0F);
	}

	@Test
	public void incrementsAndDecrements() {
		final FloatSignal signal = FloatSignal.of(0.5F);
		signal.increment();
		Assert.assertEquals(1.5F, signal.getOrDefault(), 0F);
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-0.5F, signal.getOrDefault(), 0F);
	}

	@Test
	public void multipliesAndDivides() {
		final FloatSignal signal = FloatSignal.of(3F);
		signal.multiply(2.5F);
		Assert.assertEquals(7.5F, signal.getOrDefault(), 0F);
		signal.divide(0.5F);
		Assert.assertEquals(15F, signal.getOrDefault(), 0F);
	}

	@Test(expected = ArithmeticException.class)
	public void refusesADivisionByZero() {
		FloatSignal.of(1F).divide(0F);
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Float> received = new ArrayList<>();
		final FloatSignal signal = new FloatSignal();
		signal.subscribe(received::add);
		signal.increment();
		signal.multiply(4F);
		signal.multiply(1F);
		signal.add(0F);
		Assert.assertEquals(Arrays.asList(1F, 4F), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("FloatSignal{1.5}", FloatSignal.of(1.5F).toString());
		Assert.assertEquals("FloatSignal{0.0}", new FloatSignal().toString());
	}

}