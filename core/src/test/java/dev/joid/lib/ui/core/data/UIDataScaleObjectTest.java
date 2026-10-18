package dev.joid.lib.ui.core.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataScaleObjectTest {

	@Test
	public void startsActiveWithoutLimit() {
		final UIDataScaleObject data = UIDataScaleObject.create();
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.limited());
		Assert.assertEquals(1D, data.limit(), 0D);
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataScaleObject data = AnnotationValues.read(DefaultUI.class.getAnnotation(UIDataScale.class), UIDataScaleObject.create());
		Assert.assertTrue(data.active());
		Assert.assertFalse(data.limited());
		Assert.assertEquals(1D, data.limit(), 0D);
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataScaleObject data = AnnotationValues.read(LimitedUI.class.getAnnotation(UIDataScale.class), UIDataScaleObject.create());
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.75D, data.limit(), 0D);
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(AnnotationValues.read(ChildUI.class.getAnnotation(UIDataScale.class), UIDataScaleObject.create()).limited());
	}

	@Test
	public void keepsItsDefaultsWithoutAnnotation() {
		Assert.assertTrue(AnnotationValues.read(PlainUI.class.getAnnotation(UIDataScale.class), UIDataScaleObject.create()).active());
		Assert.assertFalse(AnnotationValues.read(LimitedUI.class.getAnnotation(UIDataScale.class), UIDataScaleObject.create()).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataScaleObject data = UIDataScaleObject.create();
		Assert.assertSame(data, data.active(false).limited(true).limit(0.5D));
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.5D, data.limit(), 0D);
	}

	@Test
	public void keepsTheScaleOfTheBridgeWithoutLimit() {
		Assert.assertEquals(2.5D, UIDataScaleObject.create().limit(0.5D).apply(2.5D), 0D);
	}

	@Test
	public void capsTheScaleOfTheBridgeAtItsLimit() {
		final UIDataScaleObject data = UIDataScaleObject.create().limited(true).limit(0.75D);
		Assert.assertEquals(0.75D, data.apply(2D), 0D);
		Assert.assertEquals(0.5D, data.apply(0.5D), 0D);
	}

	@Test
	public void keepsTheScaleAtOneWhileInactive() {
		Assert.assertEquals(1D, UIDataScaleObject.create().active(false).limited(true).limit(0.5D).apply(2D), 0D);
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataScaleObject data = UIDataScaleObject.create().active(false);
		Assert.assertSame(data, AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIDataScale.class), DefaultUI.class.getAnnotation(UIDataScale.class)));
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.limited());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataScaleObject data = AnnotationValues.merge(UIDataScaleObject.create(), DefaultUI.class.getAnnotation(UIDataScale.class), LimitedUI.class.getAnnotation(UIDataScale.class));
		Assert.assertFalse(data.active());
		Assert.assertTrue(data.limited());
		Assert.assertEquals(0.75D, data.limit(), 0D);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataScaleObject(limit=0.75, active=true, limited=true)", UIDataScaleObject.create().limited(true).limit(0.75D).toString());
	}

	@UIDataScale
	public static class DefaultUI extends UI {}

	@UIDataScale(active = false, limited = true, limit = 0.75D)
	public static class LimitedUI extends UI {}

	public static class ChildUI extends LimitedUI {}

	public static class PlainUI extends UI {}

}