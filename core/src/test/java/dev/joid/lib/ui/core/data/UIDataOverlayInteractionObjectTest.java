package dev.joid.lib.ui.core.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataOverlayInteractionObjectTest {

	@Test
	public void startsWithoutInteractionAndCancelsEverything() {
		final UIDataOverlayInteractionObject data = UIDataOverlayInteractionObject.create();
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.cancelClick());
		Assert.assertTrue(data.cancelScroll());
		Assert.assertTrue(data.cancelKeyboard());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayInteractionObject data = UIDataOverlayInteractionObject.create();
		Assert.assertSame(data, data.active(true).cancelClick(false).cancelScroll(false).cancelKeyboard(false));
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.cancelClick());
		Assert.assertFalse(data.cancelScroll());
		Assert.assertFalse(data.cancelKeyboard());
	}

	@Test
	public void takesOnlyTheValuesChangedInTheAnnotation() {
		final UIDataOverlayInteraction previous = DefaultUI.class.getAnnotation(UIDataOverlay.class).interaction();
		final UIDataOverlayInteraction next = ScrollUI.class.getAnnotation(UIDataOverlay.class).interaction();
		final UIDataOverlayInteractionObject data = UIDataOverlayInteractionObject.create().active(true);
		Assert.assertSame(data, AnnotationValues.merge(data, previous, next));
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.cancelScroll());
		Assert.assertTrue(data.cancelClick());
	}

	@UIDataOverlay
	public static class DefaultUI {}

	@UIDataOverlay(interaction = @UIDataOverlayInteraction(cancelScroll = false))
	public static class ScrollUI {}

}