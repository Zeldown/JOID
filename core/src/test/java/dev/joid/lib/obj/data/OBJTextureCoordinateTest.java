package dev.joid.lib.obj.data;

import org.junit.Assert;
import org.junit.Test;

public class OBJTextureCoordinateTest {

	@Test
	public void keepsItsThreeCoordinates() {
		final OBJTextureCoordinate coordinate = new OBJTextureCoordinate(0.25F, 0.5F, 0.75F);
		Assert.assertEquals(0.25F, coordinate.getU(), 0F);
		Assert.assertEquals(0.5F, coordinate.getV(), 0F);
		Assert.assertEquals(0.75F, coordinate.getW(), 0F);
	}

	@Test
	public void hasNoDepthWithTwoCoordinates() {
		final OBJTextureCoordinate coordinate = new OBJTextureCoordinate(0.25F, 0.5F);
		Assert.assertEquals(0.25F, coordinate.getU(), 0F);
		Assert.assertEquals(0.5F, coordinate.getV(), 0F);
		Assert.assertEquals(0F, coordinate.getW(), 0F);
	}

}