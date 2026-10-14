package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BooleanSignalTest {

	@Test
	public void startsFalse() {
		final BooleanSignal signal = new BooleanSignal();
		Assert.assertFalse(signal.get());
	}

	@Test
	public void startsWithTheGivenValue() {
		final BooleanSignal signal = new BooleanSignal(true);
		Assert.assertTrue(signal.get());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertTrue(new BooleanSignal(true).set(false).reset().get());
	}

	@Test
	public void togglesItsValue() {
		final List<Boolean> received = new ArrayList<>();
		final BooleanSignal signal = new BooleanSignal();
		signal.onChange(received::add);
		signal.toggle();
		Assert.assertTrue(signal.get());
		signal.toggle();
		Assert.assertFalse(signal.get());
		Assert.assertEquals(Arrays.asList(true, false), received);
	}

	@Test
	public void togglesFromItsDefault() {
		final BooleanSignal signal = new BooleanSignal(true);
		signal.toggle();
		Assert.assertFalse(signal.get());
	}

	@Test
	public void chainsItsToggles() {
		final BooleanSignal signal = new BooleanSignal(false);
		Assert.assertSame(signal, signal.toggle().toggle().toggle());
		Assert.assertTrue(signal.get());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("BooleanSignal{true}", new BooleanSignal(true).toString());
		Assert.assertEquals("BooleanSignal{false}", new BooleanSignal().toString());
	}

}