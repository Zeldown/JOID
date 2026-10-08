package dev.joid.lib.ui.core.data.overlay;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;

public class UIDataOverlayObjectTest {

	@Test
	public void startsInactiveWithoutInteraction() {
		final UIDataOverlayObject data = new UIDataOverlayObject();
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.interaction().active());
		Assert.assertTrue(data.interaction().cancelClick());
		Assert.assertFalse(data.render().screens());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataOverlayObject data = UIDataOverlayObject.get(DefaultUI.class);
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.interaction().active());
		Assert.assertTrue(data.interaction().cancelKeyboard());
		Assert.assertFalse(data.render().always());
		Assert.assertEquals(0, data.render().zindex());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataOverlayObject data = UIDataOverlayObject.get(OverlayUI.class);
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertFalse(data.interaction().cancelClick());
		Assert.assertFalse(data.interaction().cancelScroll());
		Assert.assertFalse(data.interaction().cancelKeyboard());
		Assert.assertTrue(data.render().always());
		Assert.assertTrue(data.render().screens());
		Assert.assertEquals(3, data.render().zindex());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(UIDataOverlayObject.get(ChildUI.class).active());
	}

	@Test
	public void findsNothingWithoutAnnotation() {
		Assert.assertNull(UIDataOverlayObject.get(PlainUI.class));
	}

	@Test
	public void fallsBackOnTheDefaults() {
		Assert.assertFalse(UIDataOverlayObject.getOrDefault(PlainUI.class).active());
		Assert.assertTrue(UIDataOverlayObject.getOrDefault(OverlayUI.class).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayObject data = new UIDataOverlayObject();
		Assert.assertSame(data, data.setActive(true));
		data.interaction().setActive(true);
		data.render().setZindex(5);
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertEquals(5, data.render().zindex());
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataOverlayObject data = new UIDataOverlayObject().setActive(true);
		data.render().setZindex(4);
		Assert.assertSame(data, data.update(UIDataOverlayObject.get(DefaultUI.class), UIDataOverlayObject.get(DefaultUI.class)));
		Assert.assertTrue(data.active());
		Assert.assertEquals(4, data.render().zindex());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataOverlayObject data = new UIDataOverlayObject().update(UIDataOverlayObject.get(DefaultUI.class), UIDataOverlayObject.get(OverlayUI.class));
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertFalse(data.interaction().cancelScroll());
		Assert.assertTrue(data.render().screens());
		Assert.assertEquals(3, data.render().zindex());
	}

	@Test
	public void standsForAUIDataOverlay() {
		Assert.assertSame(UIDataOverlay.class, new UIDataOverlayObject().annotationType());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataOverlayObject(render=UIDataOverlayRenderObject(always=false, screens=false, zindex=0), interaction=UIDataOverlayInteractionObject(active=false, cancelClick=true, cancelScroll=true, cancelKeyboard=true), active=true)", new UIDataOverlayObject().setActive(true).toString());
	}

	@UIDataOverlay
	public static class DefaultUI extends UI {}

	@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true, cancelClick = false, cancelScroll = false, cancelKeyboard = false), render = @UIDataOverlayRender(always = true, screens = true, zindex = 3))
	public static class OverlayUI extends UI {}

	public static class ChildUI extends OverlayUI {}

	public static class PlainUI extends UI {}

}