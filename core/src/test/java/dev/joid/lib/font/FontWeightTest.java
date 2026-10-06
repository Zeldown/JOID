package dev.joid.lib.font;

import org.junit.Assert;
import org.junit.Test;

public class FontWeightTest {

	@Test
	public void mapsEveryNamedWeight() {
		for (final FontWeight weight : FontWeight.values()) {
			Assert.assertSame(weight, FontWeight.of(weight.getValue()));
		}
	}

	@Test
	public void clampsOutOfRangeWeights() {
		Assert.assertSame(FontWeight.THIN, FontWeight.of(0));
		Assert.assertSame(FontWeight.THIN, FontWeight.of(-300));
		Assert.assertSame(FontWeight.BLACK, FontWeight.of(1000));
	}

	@Test
	public void roundsToTheNearestWeight() {
		Assert.assertSame(FontWeight.REGULAR, FontWeight.of(449));
		Assert.assertSame(FontWeight.MEDIUM, FontWeight.of(450));
		Assert.assertSame(FontWeight.SEMI_BOLD, FontWeight.of(560));
	}

	@Test
	public void risesByAHundredFromOneWeightToTheNext() {
		for (final FontWeight weight : FontWeight.values()) {
			Assert.assertEquals((weight.ordinal() + 1) * 100, weight.getValue());
		}
	}

	@Test
	public void clampsTheExtremeIntegers() {
		Assert.assertSame(FontWeight.BLACK, FontWeight.of(Integer.MAX_VALUE));
		Assert.assertSame(FontWeight.THIN, FontWeight.of(Integer.MIN_VALUE));
	}

	@Test
	public void roundsAHalfStepUp() {
		Assert.assertSame(FontWeight.THIN, FontWeight.of(149));
		Assert.assertSame(FontWeight.EXTRA_LIGHT, FontWeight.of(150));
		Assert.assertSame(FontWeight.BLACK, FontWeight.of(850));
	}

}