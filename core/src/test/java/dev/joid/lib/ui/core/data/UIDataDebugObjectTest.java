package dev.joid.lib.ui.core.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataDebugObjectTest {

	@Test
	public void startsWithEveryToolOn() {
		final UIDataDebugObject data = new UIDataDebugObject();
		Assert.assertTrue(data.profiler());
		Assert.assertTrue(data.hotreload());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataDebugObject data = AnnotationValues.read(DefaultUI.class.getAnnotation(UIDataDebug.class), new UIDataDebugObject());
		Assert.assertTrue(data.profiler());
		Assert.assertTrue(data.hotreload());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataDebugObject data = AnnotationValues.read(QuietUI.class.getAnnotation(UIDataDebug.class), new UIDataDebugObject());
		Assert.assertFalse(data.profiler());
		Assert.assertFalse(data.hotreload());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertFalse(AnnotationValues.read(ChildUI.class.getAnnotation(UIDataDebug.class), new UIDataDebugObject()).profiler());
	}

	@Test
	public void keepsItsDefaultsWithoutAnnotation() {
		Assert.assertTrue(AnnotationValues.read(PlainUI.class.getAnnotation(UIDataDebug.class), new UIDataDebugObject()).profiler());
		Assert.assertFalse(AnnotationValues.read(QuietUI.class.getAnnotation(UIDataDebug.class), new UIDataDebugObject()).profiler());
	}

	@Test
	public void changesEveryValue() {
		final UIDataDebugObject data = new UIDataDebugObject();
		Assert.assertSame(data, data.profiler(false).hotreload(false));
		Assert.assertFalse(data.profiler());
		Assert.assertFalse(data.hotreload());
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataDebugObject data = new UIDataDebugObject().profiler(false);
		Assert.assertSame(data, AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIDataDebug.class), DefaultUI.class.getAnnotation(UIDataDebug.class)));
		Assert.assertFalse(data.profiler());
		Assert.assertTrue(data.hotreload());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataDebugObject data = AnnotationValues.merge(new UIDataDebugObject(), DefaultUI.class.getAnnotation(UIDataDebug.class), QuietUI.class.getAnnotation(UIDataDebug.class));
		Assert.assertFalse(data.profiler());
		Assert.assertFalse(data.hotreload());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataDebugObject(profiler=false, hotreload=true)", new UIDataDebugObject().profiler(false).toString());
	}

	@UIDataDebug
	public static class DefaultUI extends UI {}

	@UIDataDebug(profiler = false, hotreload = false)
	public static class QuietUI extends UI {}

	public static class ChildUI extends QuietUI {}

	public static class PlainUI extends UI {}

}