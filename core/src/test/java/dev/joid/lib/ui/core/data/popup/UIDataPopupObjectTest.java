package dev.joid.lib.ui.core.data.popup;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;

public class UIDataPopupObjectTest {

	@Test
	public void startsInactiveWithBothTransitions() {
		final UIDataPopupObject data = new UIDataPopupObject();
		Assert.assertFalse(data.active());
		Assert.assertSame(PopupTransition.IN_OUT, data.transition());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataPopupObject data = UIDataPopupObject.get(DefaultUI.class);
		Assert.assertFalse(data.active());
		Assert.assertSame(PopupTransition.IN_OUT, data.transition());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataPopupObject data = UIDataPopupObject.get(PopupUI.class);
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.IN, data.transition());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(UIDataPopupObject.get(ChildUI.class).active());
	}

	@Test
	public void findsNothingWithoutAnnotation() {
		Assert.assertNull(UIDataPopupObject.get(PlainUI.class));
	}

	@Test
	public void fallsBackOnTheDefaults() {
		Assert.assertFalse(UIDataPopupObject.getOrDefault(PlainUI.class).active());
		Assert.assertTrue(UIDataPopupObject.getOrDefault(PopupUI.class).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataPopupObject data = new UIDataPopupObject();
		Assert.assertSame(data, data.setActive(true).setTransition(PopupTransition.OUT));
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.OUT, data.transition());
	}

	@Test
	public void standsForAUIDataPopup() {
		Assert.assertSame(UIDataPopup.class, new UIDataPopupObject().annotationType());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataPopupObject(active=true, transition=NONE)", new UIDataPopupObject().setActive(true).setTransition(PopupTransition.NONE).toString());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullTransition() {
		new UIDataPopupObject().setTransition(null);
	}

	@UIDataPopup
	public static class DefaultUI extends UI {}

	@UIDataPopup(active = true, transition = PopupTransition.IN)
	public static class PopupUI extends UI {}

	public static class ChildUI extends PopupUI {}

	public static class PlainUI extends UI {}

}