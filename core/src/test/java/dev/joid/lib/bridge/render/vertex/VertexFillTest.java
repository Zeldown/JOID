package dev.joid.lib.bridge.render.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

public class VertexFillTest {

	@Test
	public void copiesACompleteBufferAsIs() {
		final VertexBuffer buffer = VertexFillTest.create(2, true, true, true);
		final ByteBuffer target = VertexFill.complete(buffer, VertexFillTest.allocate(2));
		for (int i = 0; i < 2 * VertexBuffer.STRIDE; i++) {
			Assert.assertEquals(buffer.getBuffer().get(i), target.get(i));
		}
	}

	@Test
	public void keepsThePositions() {
		final ByteBuffer target = VertexFill.complete(VertexFillTest.create(2, false, false, false), VertexFillTest.allocate(2));
		Assert.assertEquals(1F, target.getFloat(VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset()), 0F);
	}

	@Test
	public void zeroesTheMissingTextureCoordinates() {
		final ByteBuffer target = VertexFill.complete(VertexFillTest.create(1, false, true, true), VertexFillTest.allocate(1));
		Assert.assertEquals(0F, target.getFloat(VertexAttribute.TEXTURE_COORDINATE.getOffset()), 0F);
		Assert.assertEquals(0F, target.getFloat(VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4), 0F);
	}

	@Test
	public void pointsTheMissingNormalsTowardsTheViewer() {
		final ByteBuffer target = VertexFill.complete(VertexFillTest.create(1, true, true, false), VertexFillTest.allocate(1));
		Assert.assertEquals(0, target.get(VertexAttribute.NORMAL.getOffset()));
		Assert.assertEquals(0, target.get(VertexAttribute.NORMAL.getOffset() + 1));
		Assert.assertEquals(127, target.get(VertexAttribute.NORMAL.getOffset() + 2));
	}

	@Test
	public void writesFromThePositionOfTheTarget() {
		final ByteBuffer target = ByteBuffer.allocateDirect(3 * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		target.position(VertexBuffer.STRIDE);
		VertexFill.complete(VertexFillTest.create(2, false, true, true), target);
		Assert.assertEquals(VertexBuffer.STRIDE, target.position());
		Assert.assertEquals(1F, target.getFloat(2 * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset()), 0F);
		Assert.assertEquals(0F, target.getFloat(2 * VertexBuffer.STRIDE + VertexAttribute.TEXTURE_COORDINATE.getOffset()), 0F);
	}

	private static ByteBuffer allocate(final int count) {
		return ByteBuffer.allocateDirect(count * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
	}

	private static VertexBuffer create(final int count, final boolean texture, final boolean color, final boolean normal) {
		final ByteBuffer buffer = VertexFillTest.allocate(count);
		for (int i = 0; i < count * VertexBuffer.STRIDE; i++) {
			buffer.put(i, (byte) 9);
		}

		for (int i = 0; i < count; i++) {
			buffer.putFloat(i * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset(), i);
		}
		return VertexBuffer.create(buffer, count, texture, color, normal);
	}

}