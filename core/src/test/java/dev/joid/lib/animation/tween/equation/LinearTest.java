package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class LinearTest {

	@Test
	public void returnsTheTimeUnchanged() {
		for (final float t : new float[] {-0.5F, 0F, 0.25F, 0.5F, 1F, 1.5F}) {
			Assert.assertEquals(t, Linear.INOUT.compute(t), 0F);
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Linear.INOUT", Linear.INOUT.toString());
	}

}