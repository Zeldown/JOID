package dev.joid.lib.resource.resolver.impl;

import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.cache.MemoryResourceCache;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;

public class BufferedImageResourceResolverTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void supportsOnlyImages() {
		final BufferedImageResourceResolver resolver = new BufferedImageResourceResolver();
		Assert.assertTrue(resolver.supports(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
		Assert.assertFalse(resolver.supports("image.png"));
	}

	@Test
	public void decodesTheImageItself() {
		final BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(1, 0, 0xFF336699);
		final Resource resource = new BufferedImageResourceResolver().resolve(ResourceBuilder.create().cache(null), image);
		Assert.assertTrue(resource.getUniqueId(), resource.getUniqueId().startsWith("image_"));
		Assert.assertTrue(resource.getDecoder() instanceof RasterResourceDecoder);

		resource.getDecoder().decode(resource.getResourceData());
		Assert.assertEquals(3, resource.getWidth());
		Assert.assertEquals(2, resource.getHeight());
		Assert.assertEquals(0xFF336699, resource.getData()[1]);
	}

	@Test
	public void sharesTheDataOfACachedImage() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final BufferedImageResourceResolver resolver = new BufferedImageResourceResolver();
		Assert.assertSame(resolver.resolve(builder, image).getResourceData(), resolver.resolve(builder, image).getResourceData());
	}

	@Test
	public void givesTheSameImageTheSameId() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final BufferedImageResourceResolver resolver = new BufferedImageResourceResolver();
		final String first = resolver.resolve(ResourceBuilder.create().cache(null), image).getUniqueId();
		Assert.assertEquals(first, resolver.resolve(ResourceBuilder.create().cache(null), image).getUniqueId());
		Assert.assertNotEquals(first, resolver.resolve(ResourceBuilder.create().cache(null), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).getUniqueId());
	}

	@Test
	public void readsARedrawnImageAgainOnReload() {
		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xFF336699);
		final Resource resource = new BufferedImageResourceResolver().resolve(ResourceBuilder.create().cache(null), image).blocking();
		resource.prepareBind();
		Assert.assertEquals(0xFF336699, ((RecordingTexture) resource.getTexture()).getPixels()[0]);
		image.setRGB(0, 0, 0xFF996633);
		resource.prepareBind();
		Assert.assertEquals(0xFF336699, ((RecordingTexture) resource.getTexture()).getPixels()[0]);
		resource.reload().prepareBind();
		Assert.assertEquals(0xFF996633, ((RecordingTexture) resource.getTexture()).getPixels()[0]);
	}

	@Test
	public void keepsTwoImagesApart() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final BufferedImageResourceResolver resolver = new BufferedImageResourceResolver();
		Assert.assertNotSame(resolver.resolve(builder, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).getResourceData(), resolver.resolve(builder, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)).getResourceData());
	}

}