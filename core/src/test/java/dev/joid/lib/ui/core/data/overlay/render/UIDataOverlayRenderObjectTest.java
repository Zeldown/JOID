package dev.joid.lib.ui.core.data.overlay.render;

import org.junit.Assert;
import org.junit.Test;

public class UIDataOverlayRenderObjectTest {

	@Test
	public void startsHiddenBehindScreensAtZindexZero() {
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject();
		Assert.assertFalse(data.always());
		Assert.assertFalse(data.screens());
		Assert.assertEquals(0, data.zindex());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject();
		Assert.assertSame(data, data.setAlways(true).setScreens(true).setZindex(-2));
		Assert.assertTrue(data.always());
		Assert.assertTrue(data.screens());
		Assert.assertEquals(-2, data.zindex());
	}

	@Test
	public void takesOnlyTheValuesChangedInTheAnnotation() {
		final UIDataOverlayRenderObject previous = new UIDataOverlayRenderObject();
		final UIDataOverlayRenderObject next = new UIDataOverlayRenderObject().setZindex(7);
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject().setScreens(true);
		Assert.assertSame(data, data.update(previous, next));
		Assert.assertTrue(data.screens());
		Assert.assertEquals(7, data.zindex());
	}

	@Test
	public void standsForAUIDataOverlayRender() {
		Assert.assertSame(UIDataOverlayRender.class, new UIDataOverlayRenderObject().annotationType());
	}

}