package dev.joid.lib.utils.click;

import org.junit.Assert;
import org.junit.Test;

public class ClickTypeTest {

	@Test
	public void mapsEachMouseButton() {
		Assert.assertSame(ClickType.LEFT, ClickType.from(0));
		Assert.assertSame(ClickType.RIGHT, ClickType.from(1));
		Assert.assertSame(ClickType.MIDDLE, ClickType.from(2));
		Assert.assertSame(ClickType.BACK, ClickType.from(3));
		Assert.assertSame(ClickType.FORWARD, ClickType.from(4));
	}

	@Test
	public void treatsAnyOtherButtonAsOther() {
		Assert.assertSame(ClickType.OTHER, ClickType.from(5));
		Assert.assertSame(ClickType.OTHER, ClickType.from(-1));
	}

	@Test
	public void givesBackItsButton() {
		Assert.assertEquals(0, ClickType.LEFT.getButton());
		Assert.assertEquals(1, ClickType.RIGHT.getButton());
		Assert.assertEquals(2, ClickType.MIDDLE.getButton());
		Assert.assertEquals(3, ClickType.BACK.getButton());
		Assert.assertEquals(4, ClickType.FORWARD.getButton());
		Assert.assertEquals(-1, ClickType.OTHER.getButton());
	}

	@Test
	public void knowsWhichButtonItIs() {
		Assert.assertTrue(ClickType.LEFT.isLeft());
		Assert.assertTrue(ClickType.RIGHT.isRight());
		Assert.assertTrue(ClickType.MIDDLE.isMiddle());
		Assert.assertTrue(ClickType.BACK.isBack());
		Assert.assertTrue(ClickType.FORWARD.isForward());
		Assert.assertTrue(ClickType.OTHER.isOther());
		Assert.assertFalse(ClickType.RIGHT.isLeft());
		Assert.assertFalse(ClickType.LEFT.isRight());
		Assert.assertFalse(ClickType.LEFT.isMiddle());
		Assert.assertFalse(ClickType.FORWARD.isBack());
		Assert.assertFalse(ClickType.BACK.isForward());
		Assert.assertFalse(ClickType.LEFT.isOther());
	}

}