package dev.joid.lib.ui.core.data.overlay.render;

import org.junit.Assert;
import org.junit.Test;

public class UIDataOverlayRenderObjectTest {

	@Test
	public void startsHiddenAndBelowTheScreens() {
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject();
		Assert.assertFalse(data.always());
		Assert.assertFalse(data.aboveScreens());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject();
		Assert.assertSame(data, data.setAlways(true).setAboveScreens(true));
		Assert.assertTrue(data.always());
		Assert.assertTrue(data.aboveScreens());
	}

	@Test
	public void takesOnlyTheValuesChangedInTheAnnotation() {
		final UIDataOverlayRenderObject previous = new UIDataOverlayRenderObject();
		final UIDataOverlayRenderObject next = new UIDataOverlayRenderObject().setAlways(true);
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject().setAboveScreens(true);
		Assert.assertSame(data, data.update(previous, next));
		Assert.assertTrue(data.always());
		Assert.assertTrue(data.aboveScreens());
	}

	@Test
	public void standsForAUIDataOverlayRender() {
		Assert.assertSame(UIDataOverlayRender.class, new UIDataOverlayRenderObject().annotationType());
	}

}