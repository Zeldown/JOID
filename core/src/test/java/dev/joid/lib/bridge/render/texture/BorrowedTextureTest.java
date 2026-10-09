package dev.joid.lib.bridge.render.texture;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.resolver.ResourceResolver;
import lombok.NonNull;

public class BorrowedTextureTest {

	@Test
	public void readsTheSizeOfTheCurrentHandle() {
		final AtomicReference<HostImage> image = new AtomicReference<>(new HostImage(4, 2, false, true));
		final HostTexture texture = new HostTexture(image::get);
		Assert.assertEquals(4, texture.getWidth());
		Assert.assertEquals(2, texture.getHeight());
		Assert.assertTrue(texture.isAllocated());
		image.set(new HostImage(16, 8, true, true));
		Assert.assertEquals(16, texture.getWidth());
		Assert.assertEquals(8, texture.getHeight());
		Assert.assertTrue(texture.isMipmapped());
	}

	@Test
	public void isEmptyWithoutHandle() {
		final HostTexture texture = new HostTexture(() -> null);
		Assert.assertEquals(0, texture.getWidth());
		Assert.assertEquals(0, texture.getHeight());
		Assert.assertFalse(texture.isAllocated());
		Assert.assertFalse(texture.isMipmapped());
		Assert.assertFalse(texture.isValid());
	}

	@Test
	public void refusesToBeWritten() {
		final HostTexture texture = new HostTexture(() -> new HostImage(4, 4, false, true));
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
	public void keepsTheHostTextureWhenDeletedOrMipmapped() {
		final HostImage image = new HostImage(4, 4, false, true);
		final HostTexture texture = new HostTexture(() -> image);
		Assert.assertSame(texture, texture.mipmap(true));
		texture.delete();
		Assert.assertFalse(texture.isMipmapped());
		Assert.assertEquals(4, texture.getWidth());
		Assert.assertTrue(image.valid);
	}

	@Test
	public void followsTheSizeOfItsHandleInAResource() {
		final AtomicReference<HostImage> image = new AtomicReference<>(new HostImage(4, 2, false, true));
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new HostTexture(image::get), null);
		resource.prepareBind();
		Assert.assertEquals(4, resource.getWidth());
		image.set(new HostImage(32, 16, false, true));
		Assert.assertEquals(32, resource.getWidth());
		Assert.assertEquals(16, resource.getHeight());
		Assert.assertFalse(resource.isFailed());
	}

	@Test
	public void failsTheResourceOfAnInvalidHandle() {
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new HostTexture(() -> new HostImage(4, 4, false, false)), null);
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertEquals("The borrowed texture host 4x4 is not a texture of the host", resource.getResourceData().getError().getMessage());
	}

	@Test
	public void survivesTheReleaseOfItsResource() {
		final HostImage image = new HostImage(4, 4, false, true);
		final ResourceData data = new ResourceData("borrowed", null).texture(new HostTexture(() -> image));
		data.clear();
		Assert.assertTrue(image.valid);
	}

	private static final class HostImage {

		private final int     width;
		private final int     height;
		private final boolean mipmapped;
		private final boolean valid;

		private HostImage(final int width, final int height, final boolean mipmapped, final boolean valid) {
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

	private static final class HostTexture extends BorrowedTexture<HostImage> {

		private HostTexture(final Supplier<HostImage> supplier) {
			super(supplier);
		}

		@Override
		protected int getWidth(final @NonNull HostImage handle) {
			return handle.width;
		}

		@Override
		protected int getHeight(final @NonNull HostImage handle) {
			return handle.height;
		}

		@Override
		protected boolean isValid(final @NonNull HostImage handle) {
			return handle.valid;
		}

		@Override
		protected boolean isMipmapped(final @NonNull HostImage handle) {
			return handle.mipmapped;
		}

	}

}