package dev.joid.lib.ui.core.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;

public class UIDataObjectTest {

	@Test
	public void startsWithTheDefaultsOfTheAnnotation() {
		final UIDataObject data = new UIDataObject();
		Assert.assertTrue(data.pause());
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.visible());
		Assert.assertTrue(data.closeable());
		Assert.assertTrue(data.zoomable());
		Assert.assertTrue(data.background());
		Assert.assertTrue(data.projection());
		Assert.assertEquals(0D, data.zlevel(), 0D);
		Assert.assertSame(Align.CENTER, data.anchorX());
		Assert.assertSame(Align.CENTER, data.anchorY());
		Assert.assertEquals(new Color(16, 16, 16, 192), data.getBackgroundColor());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataObject data = UIDataObject.get(DefaultUI.class);
		Assert.assertTrue(data.pause());
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.visible());
		Assert.assertTrue(data.closeable());
		Assert.assertTrue(data.zoomable());
		Assert.assertTrue(data.background());
		Assert.assertTrue(data.projection());
		Assert.assertEquals(0D, data.zlevel(), 0D);
		Assert.assertSame(Align.CENTER, data.anchorX());
		Assert.assertSame(Align.CENTER, data.anchorY());
		Assert.assertEquals(new UIDataObject().getBackgroundColor(), data.getBackgroundColor());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataObject data = UIDataObject.get(CustomUI.class);
		Assert.assertFalse(data.pause());
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.visible());
		Assert.assertFalse(data.closeable());
		Assert.assertFalse(data.zoomable());
		Assert.assertFalse(data.background());
		Assert.assertFalse(data.projection());
		Assert.assertEquals(2.5D, data.zlevel(), 0D);
		Assert.assertSame(Align.START, data.anchorX());
		Assert.assertSame(Align.END, data.anchorY());
		Assert.assertEquals("#33669980", data.backgroundColor());
		Assert.assertEquals(new Color(51, 102, 153, 128), data.getBackgroundColor());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		final UIDataObject data = UIDataObject.get(ChildUI.class);
		Assert.assertFalse(data.pause());
		Assert.assertEquals(2.5D, data.zlevel(), 0D);
	}

	@Test
	public void findsNothingWithoutAnnotation() {
		Assert.assertNull(UIDataObject.get(PlainUI.class));
	}

	@Test
	public void fallsBackOnTheDefaults() {
		Assert.assertTrue(UIDataObject.getOrDefault(PlainUI.class).pause());
		Assert.assertFalse(UIDataObject.getOrDefault(CustomUI.class).pause());
	}

	@Test
	public void placesItsHorizontalAnchor() {
		final UIDataObject data = new UIDataObject();
		Assert.assertEquals(0D, data.setAnchorX(Align.START).getAnchorPositionX(), 0D);
		Assert.assertEquals(960D, data.setAnchorX(Align.CENTER).getAnchorPositionX(), 0D);
		Assert.assertEquals(1920D, data.setAnchorX(Align.END).getAnchorPositionX(), 0D);
	}

	@Test
	public void placesItsVerticalAnchor() {
		final UIDataObject data = new UIDataObject();
		Assert.assertEquals(0D, data.setAnchorY(Align.START).getAnchorPositionY(), 0D);
		Assert.assertEquals(540D, data.setAnchorY(Align.CENTER).getAnchorPositionY(), 0D);
		Assert.assertEquals(1080D, data.setAnchorY(Align.END).getAnchorPositionY(), 0D);
	}

	@Test
	public void decodesItsBackgroundColor() {
		final UIDataObject data = new UIDataObject().setBackgroundColor("#00FF00");
		Assert.assertEquals("#00FF00", data.backgroundColor());
		Assert.assertEquals(new Color(0, 255, 0, 255), data.getBackgroundColor());
	}

	@Test
	public void changesEveryValue() {
		final UIDataObject data = new UIDataObject();
		Assert.assertSame(data, data.setPause(false).setActive(false).setVisible(false).setCloseable(false).setZoomable(false).setBackground(false).setProjection(false).setZlevel(4D));
		Assert.assertFalse(data.pause());
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.visible());
		Assert.assertFalse(data.closeable());
		Assert.assertFalse(data.zoomable());
		Assert.assertFalse(data.background());
		Assert.assertFalse(data.projection());
		Assert.assertEquals(4D, data.zlevel(), 0D);
	}

	@Test
	public void standsForAUIData() {
		Assert.assertSame(UIData.class, new UIDataObject().annotationType());
	}

	@Test
	public void describesItself() {
		final String text = new UIDataObject().setAnchorX(Align.START).setZlevel(3D).toString();
		Assert.assertTrue(text, text.startsWith("UIDataObject("));
		Assert.assertTrue(text, text.contains("anchorX=START"));
		Assert.assertTrue(text, text.contains("zlevel=3.0"));
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullAnchor() {
		new UIDataObject().setAnchorX(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullBackgroundColor() {
		new UIDataObject().setBackgroundColor(null);
	}

	@UIData
	public static class DefaultUI extends UI {}

	@UIData(pause = false, active = false, visible = false, closeable = false, zoomable = false, background = false, projection = false, backgroundColor = "#33669980", zlevel = 2.5D, anchorX = Align.START, anchorY = Align.END)
	public static class CustomUI extends UI {}

	public static class ChildUI extends CustomUI {}

	public static class PlainUI extends UI {}

}