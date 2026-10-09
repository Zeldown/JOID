package dev.joid.lib.resource.resolver.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.cache.CacheBuilder;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
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
		final Resource resource = new TextureResourceResolver().resolve(ResourceBuilder.create().cache(null), texture, null);
		Assert.assertEquals("texture_" + System.identityHashCode(texture), resource.getUniqueId());
		Assert.assertSame(texture, resource.getTexture());
		Assert.assertNull(resource.getDecoder());
	}

	@Test
	public void handsTheResourceToTheCallback() {
		final List<Resource> resolved = new ArrayList<>();
		final Resource resource = new TextureResourceResolver().resolve(ResourceBuilder.create().cache(null), new Texture(), resolved::add);
		Assert.assertEquals(Collections.singletonList(resource), resolved);
	}

	@Test
	public void sharesTheDataOfACachedTexture() {
		final Texture texture = new Texture();
		final ResourceBuilder builder = ResourceBuilder.create().cache(CacheBuilder.newBuilder().build());
		final TextureResourceResolver resolver = new TextureResourceResolver();
		Assert.assertSame(resolver.resolve(builder, texture, null).getResourceData(), resolver.resolve(builder, texture, null).getResourceData());
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
		public @NonNull ITexture upload(final @NonNull int[] pixels, final int width, final int height) {
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