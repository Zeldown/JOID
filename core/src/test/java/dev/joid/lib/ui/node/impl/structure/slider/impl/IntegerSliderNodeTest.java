package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class IntegerSliderNodeTest {

	@Test
	public void spansAnInclusiveRange() {
		final Slider slider = new Slider().values(1, 5, 3);
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals(3, slider.getValue().intValue());
	}

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values(20, 30, 10, 20);
		Assert.assertEquals(Arrays.asList(30, 10, 20), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals(20, slider.getValue().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsRange() {
		new Slider().values(1, 5, 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsValues() {
		new Slider().values(40, 10, 20, 30);
	}

	public static final class Slider extends IntegerSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {}

	}

}