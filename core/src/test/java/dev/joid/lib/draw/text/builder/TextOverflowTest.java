package dev.joid.lib.draw.text.builder;

import org.junit.Assert;
import org.junit.Test;

public class TextOverflowTest {

	@Test
	public void marksTheCutWithItsSuffix() {
		Assert.assertEquals("", TextOverflow.NONE.getOverflow());
		Assert.assertEquals("...", TextOverflow.ELLIPSIS.getOverflow());
		Assert.assertEquals(".", TextOverflow.DOT.getOverflow());
		Assert.assertEquals("-", TextOverflow.HYPHEN.getOverflow());
	}

}