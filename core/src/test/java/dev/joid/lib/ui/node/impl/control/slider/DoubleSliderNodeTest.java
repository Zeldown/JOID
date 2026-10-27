package dev.joid.lib.ui.node.impl.control.slider;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.signal.Signal;

public class DoubleSliderNodeTest {

	@Test
	public void stepsFromItsMinimumToItsMaximum() {
		final Slider slider = new Slider().range(0D, 1D, 0.25D);
		Assert.assertEquals(Arrays.asList(0D, 0.25D, 0.5D, 0.75D, 1D), slider.getValueList());
		Assert.assertEquals(0D, slider.getValue(), 0D);
	}

	@Test
	public void stopsBeforeAStepPastItsMaximum() {
		Assert.assertEquals(Arrays.asList(0D, 0.5D), new Slider().range(0D, 0.75D, 0.5D).getValueList());
	}

	@Test
	public void spansAWideRangeWithoutListingEachValue() {
		final Slider slider = new Slider().range(0D, 1E9D, 1D).value(123456789D);
		Assert.assertEquals(1000000001, slider.getValueList().size());
		Assert.assertEquals(123456789, slider.getValueIndex());
		Assert.assertEquals(1E9D, slider.getValueList().get(1000000000), 0D);
	}

	@Test
	public void takesTheNearestStepOfAValueOffItsSteps() {
		Assert.assertEquals(0.25D, new Slider().<Slider>range(0D, 1D, 0.25D).value(new Signal<>(0.3D)).getValue(), 0D);
		Assert.assertEquals(1D, new Slider().<Slider>range(0D, 1D, 0.25D).value(new Signal<>(0.9D)).getValue(), 0D);
		Assert.assertEquals(0D, new Slider().<Slider>range(0D, 1D, 0.25D).value(new Signal<>(-4D)).getValue(), 0D);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOffItsSteps() {
		new Slider().range(0D, 1D, 0.25D).value(0.3D);
	}

	@Test
	public void refusesAnEmptyRange() {
		try {
			new Slider().range(1D, 0D, 0.25D);
			Assert.fail("A min above the max leaves the range empty");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The min 1.0 of the slider range is above its max 0.0, pass a min of at most 0.0", expected.getMessage());
		}
	}

	@Test
	public void reachesEachTenthUpToItsMaximum() {
		final Slider slider = new Slider().range(0D, 1D, 0.1D);
		Assert.assertEquals(11, slider.getValueList().size());
		Assert.assertTrue(slider.getValueList().contains(0.3D));
		Assert.assertTrue(slider.getValueList().contains(1D));
	}

	public static final class Slider extends DoubleSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

	}

}