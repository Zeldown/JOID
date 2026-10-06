package dev.joid.lib.obj.data;

import org.junit.Assert;
import org.junit.Test;

public class OBJVertexTest {

	@Test
	public void keepsItsThreeCoordinates() {
		final OBJVertex vertex = new OBJVertex(1F, -2F, 3.5F);
		Assert.assertEquals(1F, vertex.getX(), 0F);
		Assert.assertEquals(-2F, vertex.getY(), 0F);
		Assert.assertEquals(3.5F, vertex.getZ(), 0F);
	}

	@Test
	public void liesOnThePlaneWithTwoCoordinates() {
		final OBJVertex vertex = new OBJVertex(1F, -2F);
		Assert.assertEquals(1F, vertex.getX(), 0F);
		Assert.assertEquals(-2F, vertex.getY(), 0F);
		Assert.assertEquals(0F, vertex.getZ(), 0F);
	}

}