package dev.joid.test.contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import dev.joid.lib.resource.Resource;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public abstract class BorrowedTextureContractSuite {

	private static final int SIZE = 64;

	private static final int RED  = 0xFFFF0000;
	private static final int BLUE = 0xFF0000FF;

	private static IBorrowedTextureBackend backend;

	protected abstract @NonNull IBorrowedTextureBackend createBackend();

	@Before
	public void startBackend() {
		if (BorrowedTextureContractSuite.backend == null) {
			BorrowedTextureContractSuite.backend = this.createBackend();
			BorrowedTextureContractSuite.backend.create(BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().identity();
		render.getProjection().ortho(0D, BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE, 0D, 0D, 10000D);
		render.viewport(0, 0, BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE);
		render.frameBuffer(null);
		render.shader(null);
		render.resetTexture();
		render.blend(BlendState.NORMAL);
		render.depthTest(false);
		render.depthWrite(false);
		render.cull(false);
		render.color(1F, 1F, 1F, 1F);
		render.alphaCutoff(0F);
	}

	@AfterClass
	public static void stopBackend() {
		if (BorrowedTextureContractSuite.backend == null) {
			return;
		}

		BorrowedTextureContractSuite.backend.destroy();
		BorrowedTextureContractSuite.backend = null;
	}

	@Test
	public void drawsABorrowedTexture() {
		final Resource resource = Resource.of(BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 2, BorrowedTextureContractSuite.RED, false));
		BorrowedTextureContractSuite.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.RED);
		Assert.assertTrue(resource.getTexture() instanceof BorrowedTexture);
		Assert.assertEquals(4, resource.getWidth());
		Assert.assertEquals(2, resource.getHeight());
		Assert.assertFalse(resource.isFailed());
	}

	@Test
	public void keepsTheBorrowedTextureOnceTheResourceIsReleased() {
		final Object texture = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final Resource resource = Resource.of(texture);
		BorrowedTextureContractSuite.render(resource);
		resource.getTexture().delete();
		resource.clear();
		Assert.assertTrue(BorrowedTextureContractSuite.backend.isBorrowableTexture(texture));
		BorrowedTextureContractSuite.assertPixel(BorrowedTextureContractSuite.render(Resource.of(texture)), 32, 32, BorrowedTextureContractSuite.RED);
	}

	@Test
	public void refusesToWriteABorrowedTexture() {
		final Object handle = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final ITexture texture = BorrowedTextureContractSuite.backend.borrow(() -> handle);
		try {
			texture.allocate(8, 8);
			Assert.fail();
		} catch (final UnsupportedOperationException expected) {
			Assert.assertTrue(expected.getMessage().startsWith("A borrowed texture belongs to its host"));
		}

		try {
			texture.upload(new int[16], 4, 4);
			Assert.fail();
		} catch (final UnsupportedOperationException expected) {
			Assert.assertTrue(expected.getMessage().startsWith("A borrowed texture belongs to its host"));
		}

		Assert.assertSame(texture, texture.mipmap(true));
		Assert.assertFalse(texture.isMipmapped());
	}

	@Test
	public void readsTheMipLevelsOfTheBorrowedTexture() {
		final Object plain = BorrowedTextureContractSuite.backend.createBorrowableTexture(8, 8, BorrowedTextureContractSuite.RED, false);
		final Object mipmapped = BorrowedTextureContractSuite.backend.createBorrowableTexture(8, 8, BorrowedTextureContractSuite.RED, true);
		Assert.assertFalse(BorrowedTextureContractSuite.backend.borrow(() -> plain).isMipmapped());
		Assert.assertTrue(BorrowedTextureContractSuite.backend.borrow(() -> mipmapped).isMipmapped());
	}

	@Test
	public void restoresTheTextureParametersAfterTheFrame() {
		final Object texture = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final Map<String, String> parameters = BorrowedTextureContractSuite.backend.readBorrowableParameters(texture);
		BorrowedTextureContractSuite.render(Resource.of(texture).linear());
		Assert.assertEquals(parameters, BorrowedTextureContractSuite.backend.readBorrowableParameters(texture));
	}

	@Test
	public void followsTheHandleOfItsSupplier() {
		final Object red = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final Object blue = BorrowedTextureContractSuite.backend.createBorrowableTexture(8, 2, BorrowedTextureContractSuite.BLUE, false);
		final AtomicReference<Object> current = new AtomicReference<>(red);
		final Resource resource = Resource.of(BorrowedTextureContractSuite.backend.borrow(current::get));
		BorrowedTextureContractSuite.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.RED);
		current.set(blue);
		BorrowedTextureContractSuite.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.BLUE);
		Assert.assertEquals(8, resource.getWidth());
		Assert.assertEquals(2, resource.getHeight());
	}

	private static SnapshotImage render(final Resource resource) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		render.clearColor(0F, 0F, 0F, 1F);
		resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> render.draw(Primitive.TRIANGLES, BorrowedTextureContractSuite.quad()));
		render.endFrame();

		final SnapshotImage image = BorrowedTextureContractSuite.backend.capture(BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE);
		BorrowedTextureContractSuite.backend.present();
		return image;
	}

	private static void assertPixel(final SnapshotImage image, final int x, final int y, final int expected) {
		final int actual = image.getPixels()[x + y * image.getWidth()];
		Assert.assertEquals("Pixel " + x + "," + y, String.format("#%08X", expected), String.format("#%08X", actual));
	}

	private static VertexBuffer quad() {
		final float size = BorrowedTextureContractSuite.SIZE;
		final float[][] corners = {{0F, 0F, 0F, 0F}, {size, 0F, 1F, 0F}, {size, size, 1F, 1F}, {0F, 0F, 0F, 0F}, {size, size, 1F, 1F}, {0F, size, 0F, 1F}};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(corners.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < corners.length; i++) {
			final int offset = i * VertexBuffer.STRIDE;
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset(), corners[i][0]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 4, corners[i][1]);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset(), corners[i][2]);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, corners[i][3]);
		}
		return VertexBuffer.create(buffer, corners.length, true, false, false);
	}

}