package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class StringSliderNodeTest {

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values("low", "medium", "high").value("medium");
		Assert.assertEquals(Arrays.asList("low", "medium", "high"), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals("medium", slider.getValue());
	}

	@Test
	public void namesTheConstantsOfAnEnum() {
		final Slider slider = new Slider().values(Quality.values());
		Assert.assertEquals(Arrays.asList("LOW", "MEDIUM", "HIGH"), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals("LOW", slider.getValue());
	}

	@Test
	public void selectsAConstantOfAnEnum() {
		Assert.assertEquals("HIGH", new Slider().values(Quality.values()).value(Quality.HIGH).getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsValues() {
		new Slider().values("low", "high").value("ultra");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAConstantOutsideItsValues() {
		new Slider().values(Quality.LOW, Quality.MEDIUM).value(Quality.HIGH);
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