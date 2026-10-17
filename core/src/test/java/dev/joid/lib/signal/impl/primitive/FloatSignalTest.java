package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FloatSignalTest {

	@Test
	public void startsAtZero() {
		final FloatSignal signal = FloatSignal.create();
		Assert.assertEquals(0F, signal.get(), 0F);
	}

	@Test
	public void startsWithTheGivenValue() {
		final FloatSignal signal = FloatSignal.create(1.5F);
		Assert.assertEquals(1.5F, signal.get(), 0F);
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals(0.5F, FloatSignal.create(0.5F).set(2F).reset().get(), 0F);
	}

	@Test
	public void computesFromItsDefault() {
		final FloatSignal signal = FloatSignal.create(2F);
		signal.add(0.5F);
		Assert.assertEquals(2.5F, signal.get(), 0F);
	}

	@Test
	public void addsAndSubtracts() {
		final FloatSignal signal = FloatSignal.create(1F);
		signal.add(2.5F);
		Assert.assertEquals(3.5F, signal.get(), 0F);
		signal.subtract(0.25F);
		Assert.assertEquals(3.25F, signal.get(), 0F);
	}

	@Test
	public void incrementsAndDecrements() {
		final FloatSignal signal = FloatSignal.create(0.5F);
		signal.increment();
		Assert.assertEquals(1.5F, signal.get(), 0F);
		signal.decrement();
		signal.decrement();
		Assert.assertEquals(-0.5F, signal.get(), 0F);
	}

	@Test
	public void multipliesAndDivides() {
		final FloatSignal signal = FloatSignal.create(3F);
		signal.multiply(2.5F);
		Assert.assertEquals(7.5F, signal.get(), 0F);
		signal.divide(0.5F);
		Assert.assertEquals(15F, signal.get(), 0F);
	}

	@Test
	public void dividesByZeroLikeJava() {
		Assert.assertEquals(Float.POSITIVE_INFINITY, FloatSignal.create(1F).divide(0F).get(), 0F);
		Assert.assertEquals(Float.NEGATIVE_INFINITY, FloatSignal.create(-1F).divide(0F).get(), 0F);
		Assert.assertTrue(Float.isNaN(FloatSignal.create(0F).divide(0F).get()));
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<Float> received = new ArrayList<>();
		final FloatSignal signal = FloatSignal.create();
		signal.onChange(received::add);
		signal.increment();
		signal.multiply(4F);
		signal.multiply(1F);
		signal.add(0F);
		Assert.assertEquals(Arrays.asList(1F, 4F), received);
	}

	@Test
	public void chainsItsOperations() {
		final FloatSignal signal = FloatSignal.create(1F);
		Assert.assertSame(signal, signal.increment().add(2F).multiply(3F).subtract(1F).divide(2F).decrement());
		Assert.assertEquals(4.5F, signal.get(), 0F);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("FloatSignal{1.5}", FloatSignal.create(1.5F).toString());
		Assert.assertEquals("FloatSignal{0.0}", FloatSignal.create().toString());
	}

}