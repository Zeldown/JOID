package dev.joid.lib.utils.align;

import org.junit.Assert;
import org.junit.Test;

public class AlignTest {

	@Test
	public void knowsTheStart() {
		Assert.assertTrue(Align.START.isStart());
		Assert.assertFalse(Align.START.isEnd());
		Assert.assertFalse(Align.START.isCenter());
	}

	@Test
	public void knowsTheEnd() {
		Assert.assertTrue(Align.END.isEnd());
		Assert.assertFalse(Align.END.isStart());
		Assert.assertFalse(Align.END.isCenter());
	}

	@Test
	public void knowsTheCenter() {
		Assert.assertTrue(Align.CENTER.isCenter());
		Assert.assertFalse(Align.CENTER.isStart());
		Assert.assertFalse(Align.CENTER.isEnd());
	}

}