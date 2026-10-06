package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class StringSliderNodeTest {

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values("medium", "low", "medium", "high");
		Assert.assertEquals(Arrays.asList("low", "medium", "high"), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals("medium", slider.getValue());
	}

	@Test
	public void namesTheConstantsOfAnEnum() {
		final Slider slider = new Slider().values(Quality.HIGH, Quality.values());
		Assert.assertEquals(Arrays.asList("LOW", "MEDIUM", "HIGH"), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals("HIGH", slider.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsValues() {
		new Slider().values("ultra", "low", "high");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAConstantOutsideItsValues() {
		new Slider().values(Quality.HIGH, Quality.LOW, Quality.MEDIUM);
	}

	public static enum Quality {

		LOW,
		MEDIUM,
		HIGH;

	}

	public static final class Slider extends StringSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {}

	}

}