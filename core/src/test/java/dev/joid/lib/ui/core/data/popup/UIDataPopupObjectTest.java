package dev.joid.lib.ui.core.data.popup;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataPopupObjectTest {

	@Test
	public void startsInactiveWithBothTransitions() {
		final UIDataPopupObject data = new UIDataPopupObject();
		Assert.assertFalse(data.active());
		Assert.assertSame(PopupTransition.IN_OUT, data.transition());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataPopupObject data = AnnotationValues.read(DefaultUI.class.getAnnotation(UIDataPopup.class), new UIDataPopupObject());
		Assert.assertFalse(data.active());
		Assert.assertSame(PopupTransition.IN_OUT, data.transition());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataPopupObject data = AnnotationValues.read(PopupUI.class.getAnnotation(UIDataPopup.class), new UIDataPopupObject());
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.IN, data.transition());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(AnnotationValues.read(ChildUI.class.getAnnotation(UIDataPopup.class), new UIDataPopupObject()).active());
	}

	@Test
	public void keepsItsDefaultsWithoutAnnotation() {
		Assert.assertFalse(AnnotationValues.read(PlainUI.class.getAnnotation(UIDataPopup.class), new UIDataPopupObject()).active());
		Assert.assertTrue(AnnotationValues.read(PopupUI.class.getAnnotation(UIDataPopup.class), new UIDataPopupObject()).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataPopupObject data = new UIDataPopupObject();
		Assert.assertSame(data, data.active(true).transition(PopupTransition.OUT));
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.OUT, data.transition());
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataPopupObject data = new UIDataPopupObject().active(true);
		Assert.assertSame(data, AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIDataPopup.class), DefaultUI.class.getAnnotation(UIDataPopup.class)));
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.IN_OUT, data.transition());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataPopupObject data = AnnotationValues.merge(new UIDataPopupObject(), DefaultUI.class.getAnnotation(UIDataPopup.class), PopupUI.class.getAnnotation(UIDataPopup.class));
		Assert.assertTrue(data.active());
		Assert.assertSame(PopupTransition.IN, data.transition());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataPopupObject(active=true, transition=NONE)", new UIDataPopupObject().active(true).transition(PopupTransition.NONE).toString());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullTransition() {
		new UIDataPopupObject().transition(null);
	}

	@UIDataPopup
	public static class DefaultUI extends UI {}

	@UIDataPopup(active = true, transition = PopupTransition.IN)
	public static class PopupUI extends UI {}

	public static class ChildUI extends PopupUI {}

	public static class PlainUI extends UI {}

}