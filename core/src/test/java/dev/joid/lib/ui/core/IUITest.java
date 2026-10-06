package dev.joid.lib.ui.core;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

public class IUITest {

	private final PlainUI ui = new PlainUI();

	@Test
	public void closesByDefault() {
		Assert.assertTrue(this.ui.close());
	}

	@Test
	public void letsTheInputThroughByDefault() {
		final InternalContext context = InternalContext.create();
		this.ui.init();
		this.ui.mousePressed(10D, 20D, ClickType.LEFT, context);
		this.ui.mouseDragged(10D, 20D, ClickType.LEFT, 40L, context);
		this.ui.mouseReleased(10D, 20D, ClickType.LEFT, context);
		this.ui.mouseScroll(10D, 20D, 120, context);
		this.ui.keyPressed('a', Key.A, context);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void drawsAndUpdatesNothingByDefault() {
		this.ui.drawBackground(10D, 20D);
		this.ui.preDraw(10D, 20D);
		this.ui.postDraw(10D, 20D);
		this.ui.update();
		Assert.assertTrue(this.ui.close());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		this.ui.mousePressed(10D, 20D, null, InternalContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutContext() {
		this.ui.mousePressed(10D, 20D, ClickType.LEFT, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesADragWithoutButton() {
		this.ui.mouseDragged(10D, 20D, null, 40L, InternalContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesADragWithoutContext() {
		this.ui.mouseDragged(10D, 20D, ClickType.LEFT, 40L, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutButton() {
		this.ui.mouseReleased(10D, 20D, null, InternalContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutContext() {
		this.ui.mouseReleased(10D, 20D, ClickType.LEFT, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAScrollWithoutContext() {
		this.ui.mouseScroll(10D, 20D, 120, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutKey() {
		this.ui.keyPressed('a', null, InternalContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutContext() {
		this.ui.keyPressed('a', Key.A, null);
	}

	public static final class PlainUI implements IUI {}

}