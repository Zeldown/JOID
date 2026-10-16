package dev.joid.lib.resource.resolver.impl;

import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.cache.MemoryResourceCache;
import lombok.NonNull;

public class TextureResourceResolverTest {

	@Test
	public void supportsOnlyTextures() {
		final TextureResourceResolver resolver = new TextureResourceResolver();
		Assert.assertTrue(resolver.supports(new Texture()));
		Assert.assertFalse(resolver.supports(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
	}

	@Test
	public void wrapsTheTextureWithoutDecoder() {
		final Texture texture = new Texture();
		final Resource resource = new TextureResourceResolver().resolve(ResourceBuilder.create().cache(null), texture);
		Assert.assertTrue(resource.getUniqueId(), resource.getUniqueId().matches("texture_\\d+"));
		Assert.assertSame(texture, resource.getTexture());
		Assert.assertNull(resource.getDecoder());
	}

	@Test
	public void givesEachTextureItsOwnId() {
		final TextureResourceResolver resolver = new TextureResourceResolver();
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		final Texture texture = new Texture();
		Assert.assertEquals(resolver.resolve(builder, texture).getUniqueId(), resolver.resolve(builder, texture).getUniqueId());
		Assert.assertNotEquals(resolver.resolve(builder, texture).getUniqueId(), resolver.resolve(builder, new Texture()).getUniqueId());
	}

	@Test
	public void sharesTheDataOfACachedTexture() {
		final Texture texture = new Texture();
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final TextureResourceResolver resolver = new TextureResourceResolver();
		Assert.assertSame(resolver.resolve(builder, texture).getResourceData(), resolver.resolve(builder, texture).getResourceData());
	}

	private static final class Texture implements ITexture {

		@Override
		public @NonNull ITexture mipmap(final boolean mipmap) {
			return this;
		}

		@Override
		public @NonNull ITexture allocate(final int width, final int height) {
			return this;
		}

		@Override
		public @NonNull ITexture upload(final @NonNull int[] pixels) {
			return this;
		}

		@Override
		public int getWidth() {
			return 1;
		}

		@Override
		public int getHeight() {
			return 1;
		}

		@Override
		public boolean isMipmapped() {
			return false;
		}

		@Override
		public void delete() {}

	}

}