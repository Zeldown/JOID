package dev.joid.test.contract;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
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
		render.getState().load(new RenderState().viewport(0, 0, BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE).blend(BlendState.NORMAL).depthWrite(false));
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
		final Object texture = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 2, BorrowedTextureContractSuite.RED, false);
		final Resource resource = Resource.of(BorrowedTextureContractSuite.backend.borrow(() -> texture));
		ContractDraw.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.RED);
		Assert.assertTrue(resource.getTexture() instanceof BorrowedTexture);
		Assert.assertEquals(4, resource.getWidth());
		Assert.assertEquals(2, resource.getHeight());
		Assert.assertFalse(resource.isFailed());
	}

	@Test
	public void keepsTheBorrowedTextureOnceTheResourceIsReleased() {
		final Object texture = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final Resource resource = Resource.of(BorrowedTextureContractSuite.backend.borrow(() -> texture));
		BorrowedTextureContractSuite.render(resource);
		resource.getTexture().delete();
		resource.clear();
		Assert.assertTrue(BorrowedTextureContractSuite.backend.isBorrowableTexture(texture));
		ContractDraw.assertPixel(BorrowedTextureContractSuite.render(Resource.of(BorrowedTextureContractSuite.backend.borrow(() -> texture))), 32, 32, BorrowedTextureContractSuite.RED);
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
		BorrowedTextureContractSuite.render(Resource.of(BorrowedTextureContractSuite.backend.borrow(() -> texture)).linear());
		Assert.assertEquals(parameters, BorrowedTextureContractSuite.backend.readBorrowableParameters(texture));
	}

	@Test
	public void followsTheHandleOfItsSupplier() {
		final Object red = BorrowedTextureContractSuite.backend.createBorrowableTexture(4, 4, BorrowedTextureContractSuite.RED, false);
		final Object blue = BorrowedTextureContractSuite.backend.createBorrowableTexture(8, 2, BorrowedTextureContractSuite.BLUE, false);
		final AtomicReference<Object> current = new AtomicReference<>(red);
		final Resource resource = Resource.of(BorrowedTextureContractSuite.backend.borrow(current::get));
		ContractDraw.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.RED);
		current.set(blue);
		ContractDraw.assertPixel(BorrowedTextureContractSuite.render(resource), 32, 32, BorrowedTextureContractSuite.BLUE);
		Assert.assertEquals(8, resource.getWidth());
		Assert.assertEquals(2, resource.getHeight());
	}

	private static SnapshotImage render(final Resource resource) {
		return ContractDraw.frame(BorrowedTextureContractSuite.backend, BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE, render -> resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> render.draw(Primitive.TRIANGLES, BorrowedTextureContractSuite.quad())));
	}

	private static VertexBuffer quad() {
		return ContractDraw.quad(0F, 0F, BorrowedTextureContractSuite.SIZE, BorrowedTextureContractSuite.SIZE, 0F, true, 0, false, 0F, 1F);
	}

}