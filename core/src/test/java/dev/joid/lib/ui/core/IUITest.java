package dev.joid.lib.ui.core;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.node.callback.DispatchContext;

public class IUITest {

	private final PlainUI ui = new PlainUI();

	@Test
	public void closesByDefault() {
		Assert.assertTrue(this.ui.close());
	}

	@Test
	public void letsTheInputThroughByDefault() {
		final DispatchContext context = DispatchContext.create();
		this.ui.init();
		this.ui.mousePressed(10D, 20D, MouseButton.LEFT, context);
		this.ui.mouseDragged(10D, 20D, MouseButton.LEFT, 40L, context);
		this.ui.mouseReleased(10D, 20D, MouseButton.LEFT, context);
		this.ui.mouseScroll(10D, 20D, 0D, 1D, context);
		this.ui.keyPressed(Key.A, context);
		this.ui.charTyped('a', context);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void updatesNothingByDefault() {
		this.ui.update();
		Assert.assertTrue(this.ui.close());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		this.ui.mousePressed(10D, 20D, null, DispatchContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutContext() {
		this.ui.mousePressed(10D, 20D, MouseButton.LEFT, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesADragWithoutButton() {
		this.ui.mouseDragged(10D, 20D, null, 40L, DispatchContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesADragWithoutContext() {
		this.ui.mouseDragged(10D, 20D, MouseButton.LEFT, 40L, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutButton() {
		this.ui.mouseReleased(10D, 20D, null, DispatchContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutContext() {
		this.ui.mouseReleased(10D, 20D, MouseButton.LEFT, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAScrollWithoutContext() {
		this.ui.mouseScroll(10D, 20D, 0D, 1D, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutKey() {
		this.ui.keyPressed(null, DispatchContext.create());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutContext() {
		this.ui.keyPressed(Key.A, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesACharacterWithoutContext() {
		this.ui.charTyped('a', null);
	}

	public static final class PlainUI implements IUI {}

}