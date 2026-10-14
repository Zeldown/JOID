package dev.joid.lib.ui.core.data.overlay;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;
import dev.joid.lib.utils.annotation.AnnotationValues;

public class UIDataOverlayObjectTest {

	@Test
	public void startsInactiveWithoutInteraction() {
		final UIDataOverlayObject data = new UIDataOverlayObject();
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.interaction().active());
		Assert.assertTrue(data.interaction().cancelClick());
		Assert.assertFalse(data.render().aboveScreens());
	}

	@Test
	public void readsTheDefaultsOfTheAnnotation() {
		final UIDataOverlayObject data = AnnotationValues.read(DefaultUI.class.getAnnotation(UIDataOverlay.class), new UIDataOverlayObject());
		Assert.assertFalse(data.active());
		Assert.assertFalse(data.interaction().active());
		Assert.assertTrue(data.interaction().cancelKeyboard());
		Assert.assertFalse(data.render().always());
		Assert.assertFalse(data.render().aboveScreens());
	}

	@Test
	public void copiesEveryValueOfTheAnnotation() {
		final UIDataOverlayObject data = AnnotationValues.read(OverlayUI.class.getAnnotation(UIDataOverlay.class), new UIDataOverlayObject());
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertFalse(data.interaction().cancelClick());
		Assert.assertFalse(data.interaction().cancelScroll());
		Assert.assertFalse(data.interaction().cancelKeyboard());
		Assert.assertTrue(data.render().always());
		Assert.assertTrue(data.render().aboveScreens());
	}

	@Test
	public void inheritsTheAnnotationOfAParent() {
		Assert.assertTrue(AnnotationValues.read(ChildUI.class.getAnnotation(UIDataOverlay.class), new UIDataOverlayObject()).active());
	}

	@Test
	public void keepsItsDefaultsWithoutAnnotation() {
		Assert.assertFalse(AnnotationValues.read(PlainUI.class.getAnnotation(UIDataOverlay.class), new UIDataOverlayObject()).active());
		Assert.assertTrue(AnnotationValues.read(OverlayUI.class.getAnnotation(UIDataOverlay.class), new UIDataOverlayObject()).active());
	}

	@Test
	public void changesEveryValue() {
		final UIDataOverlayObject data = new UIDataOverlayObject();
		Assert.assertSame(data, data.active(true));
		data.interaction().active(true);
		data.render().aboveScreens(true);
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertTrue(data.render().aboveScreens());
	}

	@Test
	public void keepsItsValuesWhenTheAnnotationIsUnchanged() {
		final UIDataOverlayObject data = new UIDataOverlayObject().active(true);
		data.render().aboveScreens(true);
		Assert.assertSame(data, AnnotationValues.merge(data, DefaultUI.class.getAnnotation(UIDataOverlay.class), DefaultUI.class.getAnnotation(UIDataOverlay.class)));
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.render().aboveScreens());
	}

	@Test
	public void takesEveryValueChangedInTheAnnotation() {
		final UIDataOverlayObject data = AnnotationValues.merge(new UIDataOverlayObject(), DefaultUI.class.getAnnotation(UIDataOverlay.class), OverlayUI.class.getAnnotation(UIDataOverlay.class));
		Assert.assertTrue(data.active());
		Assert.assertTrue(data.interaction().active());
		Assert.assertFalse(data.interaction().cancelScroll());
		Assert.assertTrue(data.render().aboveScreens());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("UIDataOverlayObject(render=UIDataOverlayRenderObject(always=false, aboveScreens=false), interaction=UIDataOverlayInteractionObject(active=false, cancelClick=true, cancelScroll=true, cancelKeyboard=true), active=true)", new UIDataOverlayObject().active(true).toString());
	}

	@UIDataOverlay
	public static class DefaultUI extends UI {}

	@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true, cancelClick = false, cancelScroll = false, cancelKeyboard = false), render = @UIDataOverlayRender(always = true, aboveScreens = true))
	public static class OverlayUI extends UI {}

	public static class ChildUI extends OverlayUI {}

	public static class PlainUI extends UI {}

}