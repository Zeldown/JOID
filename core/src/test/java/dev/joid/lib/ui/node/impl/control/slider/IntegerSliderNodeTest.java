package dev.joid.lib.ui.node.impl.control.slider;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

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
	public void refusesAStepThatIsNotPositive() {
		try {
			new Slider().range(0, 10, 0);
			Assert.fail("A step of 0 never reaches the end of the range");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The step 0 of the slider range must be positive, pass a step of 1 or more", expected.getMessage());
		}
	}

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values(30, 10, 20).value(20);
		Assert.assertEquals(Arrays.asList(30, 10, 20), slider.getValueList());
		Assert.assertEquals(20, slider.getValue().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsRange() {
		new Slider().range(1, 5).value(9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyRange() {
		new Slider().range(5, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsValues() {
		new Slider().values(10, 20, 30).value(40);
	}

	public static final class Slider extends IntegerSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

	}

}