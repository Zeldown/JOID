package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class DoubleSliderNodeTest {

	@Test
	public void stepsFromItsMinimumToItsMaximum() {
		final Slider slider = new Slider().range(0D, 1D, 0.25D);
		Assert.assertEquals(Arrays.asList(0D, 0.25D, 0.5D, 0.75D, 1D), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals(0D, slider.getValue(), 0D);
	}

	@Test
	public void stopsBeforeAStepPastItsMaximum() {
		Assert.assertEquals(Arrays.asList(0D, 0.5D), new ArrayList<>(new Slider().range(0D, 0.75D, 0.5D).getValueSet()));
	}

	@Test
	public void keepsTheOrderOfItsValues() {
		final Slider slider = new Slider().values(1D, 0.25D, 0.5D, 0D).value(0.5D);
		Assert.assertEquals(Arrays.asList(1D, 0.25D, 0.5D, 0D), new ArrayList<>(slider.getValueSet()));
		Assert.assertEquals(0.5D, slider.getValue(), 0D);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOffItsSteps() {
		new Slider().range(0D, 1D, 0.25D).value(0.3D);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAValueOutsideItsValues() {
		new Slider().values(0.25D, 0.5D).value(0.75D);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyRange() {
		new Slider().range(1D, 0D, 0.25D);
	}

	@Test
	public void reachesEachTenthUpToItsMaximum() {
		final Slider slider = new Slider().range(0D, 1D, 0.1D);
		Assert.assertEquals(11, slider.getValueSet().size());
		Assert.assertTrue(slider.getValueSet().contains(0.3D));
		Assert.assertTrue(slider.getValueSet().contains(1D));
	}

	public static final class Slider extends DoubleSliderNode {

		public Slider() {
			super(0D, 0D, 400D, 50D);
		}

		@Override
		public void drawSlider(final double mouseX, final double mouseY) {}

	}

}