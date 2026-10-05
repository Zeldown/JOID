package dev.joid.lib.utils.align;

import org.junit.Assert;
import org.junit.Test;

public class AlignTest {

	@Test
	public void readsTheStartAsTheLeft() {
		Assert.assertTrue(Align.START.isStart());
		Assert.assertTrue(Align.START.isLeft());
		Assert.assertFalse(Align.START.isEnd());
		Assert.assertFalse(Align.START.isRight());
		Assert.assertFalse(Align.START.isCenter());
	}

	@Test
	public void readsTheEndAsTheRight() {
		Assert.assertTrue(Align.END.isEnd());
		Assert.assertTrue(Align.END.isRight());
		Assert.assertFalse(Align.END.isStart());
		Assert.assertFalse(Align.END.isLeft());
		Assert.assertFalse(Align.END.isCenter());
	}

	@Test
	public void knowsTheCenter() {
		Assert.assertTrue(Align.CENTER.isCenter());
		Assert.assertFalse(Align.CENTER.isStart());
		Assert.assertFalse(Align.CENTER.isLeft());
		Assert.assertFalse(Align.CENTER.isEnd());
		Assert.assertFalse(Align.CENTER.isRight());
	}

	@Test
	public void comparesWithAnotherAlignment() {
		Assert.assertTrue(Align.CENTER.is(Align.CENTER));
		Assert.assertFalse(Align.START.is(Align.END));
	}

}