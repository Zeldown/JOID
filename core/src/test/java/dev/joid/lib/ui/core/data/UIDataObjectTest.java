package dev.joid.lib.ui.core.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataObjectTest {

	@Test
	public void startsWithTheDefaultsOfTheAnnotation() {
		final UIDataObject data = new UIDataObject();
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.visible());
		Assert.assertTrue(data.closeable());
		Assert.assertTrue(data.zoomable());
		Assert.assertFalse(data.background());
		Assert.assertTrue(data.projection());
		Assert.assertEquals(0, data.zindex());
		Assert.assertEquals(0D, data.zlevel(), 0D);
		Assert.assertSame(Align.CENTER, data.anchorX());
		Assert.assertSame(Align.CENTER, data.anchorY());
		Assert.assertEquals(new Color(16, 16, 16, 192), data.getBackgroundColor());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataObject data = AnnotationValues.read(DefaultUI.class.getAnnotation(UIData.class), new UIDataObject());
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.visible());
		Assert.assertTrue(data.closeable());
		Assert.assertTrue(data.zoomable());
		Assert.assertFalse(data.background());
		Assert.assertTrue(data.projection());
		Assert.assertEquals(0, data.zindex());
		Assert.assertEquals(0D, data.zlevel(), 0D);
		Assert.assertSame(Align.CENTER, data.anchorX());
		Assert.assertSame(Align.CENTER, data.anchorY());
		Assert.assertEquals(new UIDataObject().getBackgroundColor(), data.getBackgroundColor());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataObject data = AnnotationValues.read(CustomUI.class.getAnnotation(UIData.class), new UIDataObject());
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.visible());
		Assert.assertFalse(data.closeable());
		Assert.assertFalse(data.zoomable());
		Assert.assertTrue(data.background());
		Assert.assertFalse(data.projection());
		Assert.assertEquals(3, data.zindex());
		Assert.assertEquals(2.5D, data.zlevel(), 0D);
		Assert.assertSame(Align.START, data.anchorX());
		Assert.assertSame(Align.END, data.anchorY());
		Assert.assertEquals("#33669980", data.backgroundColor());
		Assert.assertEquals(new Color(51, 102, 153, 128), data.getBackgroundColor());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		final UIDataObject data = AnnotationValues.read(ChildUI.class.getAnnotation(UIData.class), new UIDataObject());
		Assert.assertEquals(2.5D, data.zlevel(), 0D);
	}

	@Test
	public void keepsItsDefaultsWithoutAnnotation() {
		Assert.assertTrue(AnnotationValues.read(PlainUI.class.getAnnotation(UIData.class), new UIDataObject()).active());
		Assert.assertFalse(AnnotationValues.read(CustomUI.class.getAnnotation(UIData.class), new UIDataObject()).active());
	}

	@Test
	public void placesItsHorizontalAnchor() {
		final UIDataObject data = new UIDataObject();
		Assert.assertEquals(0D, data.anchorX(Align.START).getAnchorPositionX(), 0D);
		Assert.assertEquals(960D, data.anchorX(Align.CENTER).getAnchorPositionX(), 0D);
		Assert.assertEquals(1920D, data.anchorX(Align.END).getAnchorPositionX(), 0D);
	}

	@Test
	public void placesItsVerticalAnchor() {
		final UIDataObject data = new UIDataObject();
		Assert.assertEquals(0D, data.anchorY(Align.START).getAnchorPositionY(), 0D);
		Assert.assertEquals(540D, data.anchorY(Align.CENTER).getAnchorPositionY(), 0D);
		Assert.assertEquals(1080D, data.anchorY(Align.END).getAnchorPositionY(), 0D);
	}

	@Test
	public void decodesItsBackgroundColor() {
		final UIDataObject data = new UIDataObject().backgroundColor("#00FF00");
		Assert.assertEquals("#00FF00", data.backgroundColor());
		Assert.assertEquals(new Color(0, 255, 0, 255), data.getBackgroundColor());
	}

	@Test
	public void changesEveryValue() {
		final UIDataObject data = new UIDataObject();
		Assert.assertSame(data, data.active(false).visible(false).closeable(false).zoomable(false).background(true).projection(false).zindex(7).zlevel(4D));
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.visible());
		Assert.assertFalse(data.closeable());
		Assert.assertFalse(data.zoomable());
		Assert.assertTrue(data.background());
		Assert.assertFalse(data.projection());
		Assert.assertEquals(7, data.zindex());
		Assert.assertEquals(4D, data.zlevel(), 0D);
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataObject data = new UIDataObject().zlevel(4D).visible(false).anchorX(Align.END).backgroundColor("#00FF00");
		Assert.assertSame(data, AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIData.class), DefaultUI.class.getAnnotation(UIData.class)));
		Assert.assertEquals(4D, data.zlevel(), 0D);
		Assert.assertFalse(data.visible());
		Assert.assertSame(Align.END, data.anchorX());
		Assert.assertEquals(new Color(0, 255, 0, 255), data.getBackgroundColor());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataObject data = new UIDataObject().zlevel(4D).anchorY(Align.START);
		AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIData.class), CustomUI.class.getAnnotation(UIData.class));
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.visible());
		Assert.assertFalse(data.closeable());
		Assert.assertFalse(data.zoomable());
		Assert.assertTrue(data.background());
		Assert.assertFalse(data.projection());
		Assert.assertEquals(3, data.zindex());
		Assert.assertEquals(2.5D, data.zlevel(), 0D);
		Assert.assertSame(Align.START, data.anchorX());
		Assert.assertSame(Align.END, data.anchorY());
		Assert.assertEquals(new Color(51, 102, 153, 128), data.getBackgroundColor());
	}

	@Test
	public void describesItself() {
		final String text = new UIDataObject().anchorX(Align.START).zlevel(3D).toString();
		Assert.assertTrue(text, text.startsWith("UIDataObject("));
		Assert.assertTrue(text, text.contains("anchorX=START"));
		Assert.assertTrue(text, text.contains("zlevel=3.0"));
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullAnchor() {
		new UIDataObject().anchorX(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullBackgroundColor() {
		new UIDataObject().backgroundColor(null);
	}

	@UIData
	public static class DefaultUI extends UI {}

	@UIData(active = false, visible = false, closeable = false, zoomable = false, background = true, projection = false, backgroundColor = "#33669980", zindex = 3, zlevel = 2.5D, anchorX = Align.START, anchorY = Align.END)
	public static class CustomUI extends UI {}

	public static class ChildUI extends CustomUI {}

	public static class PlainUI extends UI {}

}