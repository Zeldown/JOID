package dev.joid.lib.utils.align;

import org.junit.Assert;
import org.junit.Test;

public class AxisTest {

	@Test
	public void tellsWhetherItIsHorizontal() {
		Assert.assertTrue(Axis.HORIZONTAL.isHorizontal());
		Assert.assertFalse(Axis.VERTICAL.isHorizontal());
	}

}