package dev.joid.lib.ui.core.data.scale;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;

public class UIDataScaleObjectTest {

	@Test
	public void startsActiveWithoutLimit() {
		final UIDataScaleObject data = new UIDataScaleObject();
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.limited());
		Assert.assertEquals(1D, data.limit(), 0D);
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataScaleObject data = UIDataScaleObject.get(DefaultUI.class);
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.limited());
		Assert.assertEquals(1D, data.limit(), 0D);
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataScaleObject data = UIDataScaleObject.get(LimitedUI.class);
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.75D, data.limit(), 0D);
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(UIDataScaleObject.get(ChildUI.class).limited());
	}

	@Test
	public void findsNothingWithoutAnnotation() {
		Assert.assertNull(UIDataScaleObject.get(PlainUI.class));
	}

	@Test
	public void fallsBackOnTheDefaults() {
		Assert.assertTrue(UIDataScaleObject.getOrDefault(PlainUI.class).active());
		Assert.assertFalse(UIDataScaleObject.getOrDefault(LimitedUI.class).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataScaleObject data = new UIDataScaleObject();
		Assert.assertSame(data, data.setActive(false).setLimited(true).setLimit(0.5D));
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.5D, data.limit(), 0D);
	}

	@Test
	public void keepsTheScaleOfTheBridgeWithoutLimit() {
		Assert.assertEquals(2.5D, new UIDataScaleObject().setLimit(0.5D).apply(2.5D), 0D);
	}

	@Test
	public void capsTheScaleOfTheBridgeAtItsLimit() {
		final UIDataScaleObject data = new UIDataScaleObject().setLimited(true).setLimit(0.75D);
		Assert.assertEquals(0.75D, data.apply(2D), 0D);
		Assert.assertEquals(0.5D, data.apply(0.5D), 0D);
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataScaleObject data = new UIDataScaleObject().setActive(false);
		Assert.assertSame(data, data.update(UIDataScaleObject.get(DefaultUI.class), UIDataScaleObject.get(DefaultUI.class)));
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.limited());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataScaleObject data = new UIDataScaleObject().update(UIDataScaleObject.get(DefaultUI.class), UIDataScaleObject.get(LimitedUI.class));
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.75D, data.limit(), 0D);
	}

	@Test
	public void standsForAUIDataScale() {
		Assert.assertSame(UIDataScale.class, new UIDataScaleObject().annotationType());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataScaleObject(active=true, limited=true, limit=0.75)", new UIDataScaleObject().setLimited(true).setLimit(0.75D).toString());
	}

	@UIDataScale
	public static class DefaultUI extends UI {}

	@UIDataScale(active = false, limited = true, limit = 0.75D)
	public static class LimitedUI extends UI {}

	public static class ChildUI extends LimitedUI {}

	public static class PlainUI extends UI {}

}