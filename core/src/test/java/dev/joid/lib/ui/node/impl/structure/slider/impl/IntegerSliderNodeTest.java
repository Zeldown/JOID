package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class IntegerSliderNodeTest {

	@Test
	public void spansAnInclusiveRange() {
		final Slider slider = new Slider().range(1, 5);
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals(1, slider.getValue().intValue());
	}

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values(30, 10, 20).value(20);
		Assert.assertEquals(Arrays.asList(30, 10, 20), new ArrayList<>(slider.getValueSet()));
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

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {}

	}

}