package dev.joid.lib.utils.box;

import org.junit.Assert;
import org.junit.Test;

public class BoundingBoxTest {

	@Test
	public void spansFromItsOriginOverItsSize() {
		final BoundingBox box = BoundingBox.create(10D, 20D, 30D, 40D);
		Assert.assertEquals(10D, box.getMinX(), 0D);
		Assert.assertEquals(20D, box.getMinY(), 0D);
		Assert.assertEquals(40D, box.getMaxX(), 0D);
		Assert.assertEquals(60D, box.getMaxY(), 0D);
		Assert.assertEquals(30D, box.getWidth(), 0D);
		Assert.assertEquals(40D, box.getHeight(), 0D);
	}

	@Test
	public void expandsOnEverySide() {
		final BoundingBox box = BoundingBox.create(10D, 20D, 30D, 40D);
		Assert.assertSame(box, box.expand(5D));
		Assert.assertEquals(5D, box.getMinX(), 0D);
		Assert.assertEquals(15D, box.getMinY(), 0D);
		Assert.assertEquals(45D, box.getMaxX(), 0D);
		Assert.assertEquals(65D, box.getMaxY(), 0D);
	}

	@Test
	public void contractsOnEverySide() {
		final BoundingBox box = BoundingBox.create(10D, 20D, 30D, 40D);
		Assert.assertSame(box, box.contract(5D));
		Assert.assertEquals(15D, box.getMinX(), 0D);
		Assert.assertEquals(25D, box.getMinY(), 0D);
		Assert.assertEquals(35D, box.getMaxX(), 0D);
		Assert.assertEquals(55D, box.getMaxY(), 0D);
	}

	@Test
	public void copiesItsBounds() {
		final BoundingBox box = BoundingBox.create(10D, 20D, 30D, 40D);
		final BoundingBox copy = box.copy();
		Assert.assertNotSame(box, copy);
		Assert.assertEquals(10D, copy.getMinX(), 0D);
		Assert.assertEquals(60D, copy.getMaxY(), 0D);
		copy.expand(5D);
		Assert.assertEquals(30D, box.getWidth(), 0D);
		Assert.assertEquals(40D, copy.getWidth(), 0D);
	}

	@Test
	public void movesEachBound() {
		final BoundingBox box = BoundingBox.create(0D, 0D, 10D, 10D);
		box.setMinX(2D);
		box.setMinY(4D);
		box.setMaxX(12D);
		box.setMaxY(24D);
		Assert.assertEquals(10D, box.getWidth(), 0D);
		Assert.assertEquals(20D, box.getHeight(), 0D);
	}

}