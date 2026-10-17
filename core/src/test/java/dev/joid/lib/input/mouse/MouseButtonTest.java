package dev.joid.lib.input.mouse;

import org.junit.Assert;
import org.junit.Test;

public class MouseButtonTest {

	@Test
	public void mapsEachMouseButton() {
		Assert.assertSame(MouseButton.LEFT, MouseButton.of(0));
		Assert.assertSame(MouseButton.RIGHT, MouseButton.of(1));
		Assert.assertSame(MouseButton.MIDDLE, MouseButton.of(2));
		Assert.assertSame(MouseButton.BACK, MouseButton.of(3));
		Assert.assertSame(MouseButton.FORWARD, MouseButton.of(4));
	}

	@Test
	public void treatsAnyOtherButtonAsOther() {
		Assert.assertSame(MouseButton.OTHER, MouseButton.of(5));
		Assert.assertSame(MouseButton.OTHER, MouseButton.of(-1));
	}

	@Test
	public void givesBackItsButton() {
		Assert.assertEquals(0, MouseButton.LEFT.getButton());
		Assert.assertEquals(1, MouseButton.RIGHT.getButton());
		Assert.assertEquals(2, MouseButton.MIDDLE.getButton());
		Assert.assertEquals(3, MouseButton.BACK.getButton());
		Assert.assertEquals(4, MouseButton.FORWARD.getButton());
		Assert.assertEquals(-1, MouseButton.OTHER.getButton());
	}

	@Test
	public void knowsWhichButtonItIs() {
		Assert.assertTrue(MouseButton.LEFT.isLeft());
		Assert.assertTrue(MouseButton.RIGHT.isRight());
		Assert.assertTrue(MouseButton.MIDDLE.isMiddle());
		Assert.assertTrue(MouseButton.BACK.isBack());
		Assert.assertTrue(MouseButton.FORWARD.isForward());
		Assert.assertTrue(MouseButton.OTHER.isOther());
		Assert.assertFalse(MouseButton.RIGHT.isLeft());
		Assert.assertFalse(MouseButton.LEFT.isRight());
		Assert.assertFalse(MouseButton.LEFT.isMiddle());
		Assert.assertFalse(MouseButton.FORWARD.isBack());
		Assert.assertFalse(MouseButton.BACK.isForward());
		Assert.assertFalse(MouseButton.LEFT.isOther());
	}

}