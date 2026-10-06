package dev.joid.lib.ui.core.data.debug;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;

public class UIDataDebugObjectTest {

	@Test
	public void startsWithEveryToolOn() {
		final UIDataDebugObject data = new UIDataDebugObject();
		Assert.assertTrue(data.profiler());
		Assert.assertTrue(data.hotreload());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataDebugObject data = UIDataDebugObject.get(DefaultUI.class).get();
		Assert.assertTrue(data.profiler());
		Assert.assertTrue(data.hotreload());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataDebugObject data = UIDataDebugObject.get(QuietUI.class).get();
		Assert.assertFalse(data.profiler());
		Assert.assertFalse(data.hotreload());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertFalse(UIDataDebugObject.get(ChildUI.class).get().profiler());
	}

	@Test
	public void findsNothingWithoutAnnotation() {
		Assert.assertFalse(UIDataDebugObject.get(PlainUI.class).isPresent());
	}

	@Test
	public void fallsBackOnTheDefaults() {
		Assert.assertTrue(UIDataDebugObject.getOrDefault(PlainUI.class).profiler());
		Assert.assertFalse(UIDataDebugObject.getOrDefault(QuietUI.class).profiler());
	}

	@Test
	public void changesEveryValue() {
		final UIDataDebugObject data = new UIDataDebugObject();
		Assert.assertSame(data, data.setProfiler(false).setHotreload(false));
		Assert.assertFalse(data.profiler());
		Assert.assertFalse(data.hotreload());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataDebugObject(profiler=false, hotreload=true)", new UIDataDebugObject().setProfiler(false).toString());
	}

	@Test
	public void standsForAUIDataDebug() {
		Assert.assertSame(UIDataDebug.class, new UIDataDebugObject().annotationType());
	}

	@UIDataDebug
	public static class DefaultUI extends UI {}

	@UIDataDebug(profiler = false, hotreload = false)
	public static class QuietUI extends UI {}

	public static class ChildUI extends QuietUI {}

	public static class PlainUI extends UI {}

}