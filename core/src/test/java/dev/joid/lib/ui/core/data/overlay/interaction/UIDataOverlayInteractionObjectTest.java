package dev.joid.lib.ui.core.data.overlay.interaction;

import org.junit.Assert;
import org.junit.Test;

public class UIDataOverlayInteractionObjectTest {

	@Test
	public void startsWithoutInteractionAndCancelsEverything() {
		final UIDataOverlayInteractionObject data = new UIDataOverlayInteractionObject();
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.cancelClick());
		Assert.assertTrue(data.cancelScroll());
		Assert.assertTrue(data.cancelKeyboard());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayInteractionObject data = new UIDataOverlayInteractionObject();
		Assert.assertSame(data, data.setActive(true).setCancelClick(false).setCancelScroll(false).setCancelKeyboard(false));
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.cancelClick());
		Assert.assertFalse(data.cancelScroll());
		Assert.assertFalse(data.cancelKeyboard());
	}

	@Test
	public void takesOnlyTheValuesChangedInTheAnnotation() {
		final UIDataOverlayInteractionObject previous = new UIDataOverlayInteractionObject();
		final UIDataOverlayInteractionObject next = new UIDataOverlayInteractionObject().setCancelScroll(false);
		final UIDataOverlayInteractionObject data = new UIDataOverlayInteractionObject().setActive(true);
		Assert.assertSame(data, data.update(previous, next));
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.cancelScroll());
		Assert.assertTrue(data.cancelClick());
	}

	@Test
	public void standsForAUIDataOverlayInteraction() {
		Assert.assertSame(UIDataOverlayInteraction.class, new UIDataOverlayInteractionObject().annotationType());
	}

}