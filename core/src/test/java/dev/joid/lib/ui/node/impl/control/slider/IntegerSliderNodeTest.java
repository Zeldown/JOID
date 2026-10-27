package dev.joid.lib.ui.node.impl.control.slider;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.signal.Signal;

public class IntegerSliderNodeTest {

	@Test
	public void spansAnInclusiveRange() {
		final Slider slider = new Slider().range(1, 5);
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), slider.getValueList());
		Assert.assertEquals(1, slider.getValue().intValue());
	}

	@Test
	public void spansARangeByItsStep() {
		final Slider slider = new Slider().range(0, 10, 4);
		Assert.assertEquals(Arrays.asList(0, 4, 8), slider.getValueList());
		Assert.assertEquals(0, slider.getValue().intValue());
	}

	@Test
	public void spansAWholeRangeWithoutListingEachValue() {
		final Slider slider = new Slider().range(1, Integer.MAX_VALUE).value(1000000);
		Assert.assertEquals(Integer.MAX_VALUE, slider.getValueList().size());
		Assert.assertEquals(999999, slider.getValueIndex());
		Assert.assertEquals(Integer.MAX_VALUE, slider.getValueList().get(Integer.MAX_VALUE - 1).intValue());
	}

	@Test
	public void refusesARangeOfMoreValuesThanAListHolds() {
		try {
			new Slider().range(0, Integer.MAX_VALUE);
			Assert.fail("A list holds at most Integer.MAX_VALUE values");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The slider range from 0 to 2147483647 by 1 holds more than 2147483647 values, pass a narrower range or a larger step", expected.getMessage());
		}
	}

	@Test
	public void takesTheNearestOptionOfAValueOutsideItsOptions() {
		Assert.assertEquals(8, new Slider().<Slider>range(0, 10, 4).value(new Signal<>(7)).getValue().intValue());
		Assert.assertEquals(0, new Slider().<Slider>range(0, 10, 4).value(new Signal<>(-3)).getValue().intValue());
		Assert.assertEquals(8, new Slider().<Slider>range(0, 10, 4).value(new Signal<>(50)).getValue().intValue());
	}

	@Test
	public void takesTheSmallestOfTwoNearestOptions() {
		Assert.assertEquals(0, new Slider().<Slider>values(10, 0).value(new Signal<>(5)).getValue().intValue());
		Assert.assertEquals(0, new Slider().<Slider>values(0, 10).value(new Signal<>(5)).getValue().intValue());
		Assert.assertEquals(4, new Slider().<Slider>range(0, 10, 4).value(new Signal<>(6)).getValue().intValue());
	}

	@Test
	public void hashesItsRangeLikeAListOfItsValues() {
		final List<Integer> values = new Slider().range(0, 3).getValueList();
		Assert.assertEquals(Arrays.asList(0, 1, 2, 3), values);
		Assert.assertEquals(Arrays.asList(0, 1, 2, 3).hashCode(), values.hashCode());
	}

	@Test
	public void refusesAStepThatIsNotPositive() {
		try {
			new Slider().range(0, 10, 0);
			Assert.fail("A step of 0 never reaches the end of the range");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The step 0 of the slider range must be positive, pass a step of 1 or more", expected.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsRange() {
		new Slider().range(1, 5).value(9);
	}

	@Test
	public void refusesAnEmptyRange() {
		try {
			new Slider().range(5, 1);
			Assert.fail("A min above the max leaves the range empty");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The min 5 of the slider range is above its max 1, pass a min of at most 1", expected.getMessage());
		}
	}

	public static final class Slider extends IntegerSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

	}

}