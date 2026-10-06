package dev.joid.lib.ui.node.hover;

import org.junit.Assert;
import org.junit.Test;

public class HoverElementTest {

	@Test
	public void sitsAtTheOriginWithoutSize() {
		final HoverElement element = (node, mouseX, mouseY) -> {};
		Assert.assertEquals(0D, element.getX(), 0D);
		Assert.assertEquals(0D, element.getY(), 0D);
		Assert.assertEquals(0D, element.getWidth(), 0D);
		Assert.assertEquals(0D, element.getHeight(), 0D);
	}

}