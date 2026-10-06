package dev.joid.lib.resource.dto.resolver;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import lombok.NonNull;

public class ResourceResolverTest {

	@Test
	public void supportsTexturesAndImages() {
		Assert.assertTrue(ResourceResolver.supports(new Texture()));
		Assert.assertTrue(ResourceResolver.supports(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
	}

	@Test
	public void supportsNoOtherInput() {
		Assert.assertFalse(ResourceResolver.supports("image.png"));
		Assert.assertFalse(ResourceResolver.supports(new Object()));
	}

	@Test
	public void resolvesATexture() {
		final Texture texture = new Texture();
		Assert.assertSame(texture, ResourceResolver.resolve(ResourceBuilder.create().cache(null), texture, null).getTexture());
	}

	@Test
	public void resolvesAnImage() {
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), null);
		Assert.assertTrue(resource.getDecoder() instanceof RasterResourceDecoder);
	}

	@Test
	public void handsTheResourceToTheCallback() {
		final List<Resource> resolved = new ArrayList<>();
		final Resource resource = ResourceResolver.resolve(ResourceBuilder.create().cache(null), new Texture(), resolved::add);
		Assert.assertEquals(Collections.singletonList(resource), resolved);
	}

	@Test
	public void prefersTheLastRegisteredResolver() {
		ResourceResolver.register(new SpecialResolver());
		Assert.assertTrue(ResourceResolver.supports(new SpecialImage()));
		Assert.assertEquals("special", ResourceResolver.resolve(ResourceBuilder.create().cache(null), new SpecialImage(), null).getUniqueId());
		Assert.assertTrue(ResourceResolver.resolve(ResourceBuilder.create().cache(null), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), null).getDecoder() instanceof RasterResourceDecoder);
	}

	@Test
	public void namesTheTypeOfAnUnknownInput() {
		try {
			ResourceResolver.resolve(ResourceBuilder.create().cache(null), "image.png", null);
			Assert.fail("An unknown input must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("No resolver found for input of type java.lang.String", expected.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullResolver() {
		ResourceResolver.register(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToSupportANullInput() {
		ResourceResolver.supports(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToResolveWithoutBuilder() {
		ResourceResolver.resolve(null, new Texture(), null);
	}

	private static final class SpecialImage extends BufferedImage {

		private SpecialImage() {
			super(1, 1, BufferedImage.TYPE_INT_ARGB);
		}

	}

	private static final class SpecialResolver implements IResourceResolver {

		@Override
		public boolean supports(final @NonNull Object input) {
			return input instanceof SpecialImage;
		}

		@Override
		public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
			return builder.compute("special", () -> new ResourceData("special", null));
		}

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