package dev.joid.lib.bridge.render.texture;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.NonNull;

public class BorrowedTextureTest {

	@Test
	public void readsTheSizeOfTheCurrentHandle() {
		final AtomicReference<BorrowableImage> image = new AtomicReference<>(new BorrowableImage(4, 2, false, true));
		final BorrowableTexture texture = new BorrowableTexture(image::get);
		Assert.assertEquals(4, texture.getWidth());
		Assert.assertEquals(2, texture.getHeight());
		Assert.assertTrue(texture.isAllocated());
		image.set(new BorrowableImage(16, 8, true, true));
		Assert.assertEquals(16, texture.getWidth());
		Assert.assertEquals(8, texture.getHeight());
		Assert.assertTrue(texture.isMipmapped());
	}

	@Test
	public void isEmptyWithoutHandle() {
		final BorrowableTexture texture = new BorrowableTexture(() -> null);
		Assert.assertEquals(0, texture.getWidth());
		Assert.assertEquals(0, texture.getHeight());
		Assert.assertFalse(texture.isAllocated());
		Assert.assertFalse(texture.isMipmapped());
		Assert.assertFalse(texture.isValid());
	}

	@Test
	public void refusesToBeWritten() {
		final BorrowableTexture texture = new BorrowableTexture(() -> new BorrowableImage(4, 4, false, true));
		try {
			texture.allocate(8, 8);
			Assert.fail();
		} catch (final UnsupportedOperationException expected) {
			Assert.assertEquals("A borrowed texture belongs to its host: JOID reads it but never allocates it, create a texture with IRenderBridge.createTexture() to draw your own pixels", expected.getMessage());
		}

		try {
			texture.upload(new int[16], 4, 4);
			Assert.fail();
		} catch (final UnsupportedOperationException expected) {
			Assert.assertEquals("A borrowed texture belongs to its host: JOID reads it but never writes it, create a texture with IRenderBridge.createTexture() to draw your own pixels", expected.getMessage());
		}
	}

	@Test
	public void keepsTheBorrowedTextureWhenDeletedOrMipmapped() {
		final BorrowableImage image = new BorrowableImage(4, 4, false, true);
		final BorrowableTexture texture = new BorrowableTexture(() -> image);
		Assert.assertSame(texture, texture.mipmap(true));
		texture.delete();
		Assert.assertFalse(texture.isMipmapped());
		Assert.assertEquals(4, texture.getWidth());
		Assert.assertTrue(image.valid);
	}

	@Test
	public void followsTheSizeOfItsHandleInAResource() {
		final AtomicReference<BorrowableImage> image = new AtomicReference<>(new BorrowableImage(4, 2, false, true));
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new BorrowableTexture(image::get), null);
		resource.prepareBind();
		Assert.assertEquals(4, resource.getWidth());
		image.set(new BorrowableImage(32, 16, false, true));
		Assert.assertEquals(32, resource.getWidth());
		Assert.assertEquals(16, resource.getHeight());
		Assert.assertFalse(resource.isFailed());
	}

	@Test
	public void failsTheResourceOfAnInvalidHandle() {
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new BorrowableTexture(() -> new BorrowableImage(4, 4, false, false)), null);
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertEquals("The borrowed texture host 4x4 is not a texture of the host", resource.getResourceData().getError().getMessage());
	}

	@Test
	public void survivesTheReleaseOfItsResource() {
		final BorrowableImage image = new BorrowableImage(4, 4, false, true);
		final ResourceData data = new ResourceData("borrowed", null).texture(new BorrowableTexture(() -> image));
		data.clear();
		Assert.assertTrue(image.valid);
	}

	private static final class BorrowableImage {

		private final int     width;
		private final int     height;
		private final boolean valid;
		private final boolean mipmapped;

		private BorrowableImage(final int width, final int height, final boolean mipmapped, final boolean valid) {
			this.width = width;
			this.height = height;
			this.mipmapped = mipmapped;
			this.valid = valid;
		}

		@Override
		public @NonNull String toString() {
			return "host " + this.width + "x" + this.height;
		}

	}

	private static final class BorrowableTexture extends BorrowedTexture<BorrowableImage> {

		private BorrowableTexture(final Supplier<BorrowableImage> supplier) {
			super(supplier);
		}

		@Override
		protected int getWidth(final @NonNull BorrowableImage handle) {
			return handle.width;
		}

		@Override
		protected int getHeight(final @NonNull BorrowableImage handle) {
			return handle.height;
		}

		@Override
		protected boolean isValid(final @NonNull BorrowableImage handle) {
			return handle.valid;
		}

		@Override
		protected boolean isMipmapped(final @NonNull BorrowableImage handle) {
			return handle.mipmapped;
		}

	}

}