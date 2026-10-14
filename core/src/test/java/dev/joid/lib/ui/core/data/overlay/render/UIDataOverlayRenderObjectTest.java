package dev.joid.lib.ui.core.data.overlay.render;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.utils.annotation.AnnotationValues;

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
		Assert.assertSame(data, data.always(true).aboveScreens(true));
		Assert.assertTrue(data.always());
		Assert.assertTrue(data.aboveScreens());
	}

	@Test
	public void takesOnlyTheValuesChangedInTheAnnotation() {
		final UIDataOverlayRender previous = DefaultUI.class.getAnnotation(UIDataOverlay.class).render();
		final UIDataOverlayRender next = AlwaysUI.class.getAnnotation(UIDataOverlay.class).render();
		final UIDataOverlayRenderObject data = new UIDataOverlayRenderObject().aboveScreens(true);
		Assert.assertSame(data, AnnotationValues.merge(data, previous, next));
		Assert.assertTrue(data.always());
		Assert.assertTrue(data.aboveScreens());
	}

	@UIDataOverlay
	public static class DefaultUI {}

	@UIDataOverlay(render = @UIDataOverlayRender(always = true))
	public static class AlwaysUI {}

}