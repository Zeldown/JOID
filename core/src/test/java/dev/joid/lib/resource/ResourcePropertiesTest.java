package dev.joid.lib.resource;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.texture.TextureFilter;

public class ResourcePropertiesTest {

	@Test
	public void leavesTheMipmapsToTheDrawByDefault() {
		Assert.assertNull(ResourceProperties.create().getMipmap());
	}

	@Test
	public void keepsAnExplicitMipmapChoice() {
		final ResourceProperties properties = ResourceProperties.create().mipmap(false);
		Assert.assertFalse(properties.getMipmap());
		Assert.assertFalse(properties.copy().getMipmap());
		Assert.assertTrue(ResourceProperties.create().copy(properties.mipmap(true)).getMipmap());
	}

	@Test
	public void startsBlockingAndNearestWithoutTextureCoordinates() {
		final ResourceProperties properties = ResourceProperties.create();
		Assert.assertFalse(properties.isAsync());
		Assert.assertSame(TextureFilter.NEAREST, properties.getInterpolation());
		Assert.assertNull(properties.getTextureCoords());
	}

	@Test
	public void switchesBetweenAsyncAndBlocking() {
		final ResourceProperties properties = ResourceProperties.create();
		Assert.assertSame(properties, properties.async());
		Assert.assertTrue(properties.isAsync());
		Assert.assertSame(properties, properties.blocking());
		Assert.assertFalse(properties.isAsync());
	}

	@Test
	public void switchesTheInterpolation() {
		final ResourceProperties properties = ResourceProperties.create();
		Assert.assertSame(TextureFilter.LINEAR, properties.linear().getInterpolation());
		Assert.assertSame(TextureFilter.NEAREST, properties.nearest().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, properties.interpolation(TextureFilter.LINEAR).getInterpolation());
	}

	@Test
	public void keepsTheTextureCoordinates() {
		Assert.assertArrayEquals(new double[] {0.25D, 0.5D, 0.75D, 1D}, ResourceProperties.create().textureCoords(0.25D, 0.5D, 0.75D, 1D).getTextureCoords(), 0D);
	}

	@Test
	public void copiesEveryProperty() {
		final ResourceProperties properties = ResourceProperties.create().async().linear().mipmap(true).textureCoords(0D, 0D, 0.5D, 0.5D);
		final ResourceProperties copy = properties.copy();
		Assert.assertNotSame(properties, copy);
		Assert.assertTrue(copy.isAsync());
		Assert.assertSame(TextureFilter.LINEAR, copy.getInterpolation());
		Assert.assertTrue(copy.getMipmap());
		Assert.assertArrayEquals(new double[] {0D, 0D, 0.5D, 0.5D}, copy.getTextureCoords(), 0D);
	}

	@Test
	public void changesACopyIndependently() {
		final ResourceProperties properties = ResourceProperties.create();
		properties.copy().async().linear().mipmap(true);
		Assert.assertFalse(properties.isAsync());
		Assert.assertSame(TextureFilter.NEAREST, properties.getInterpolation());
		Assert.assertNull(properties.getMipmap());
	}

	@Test
	public void takesEveryPropertyOfAnother() {
		final ResourceProperties other = ResourceProperties.create().async().linear().textureCoords(0D, 0D, 1D, 1D);
		final ResourceProperties properties = ResourceProperties.create();
		Assert.assertSame(properties, properties.copy(other));
		Assert.assertTrue(properties.isAsync());
		Assert.assertSame(TextureFilter.LINEAR, properties.getInterpolation());
		Assert.assertNull(properties.getMipmap());
		Assert.assertArrayEquals(new double[] {0D, 0D, 1D, 1D}, properties.getTextureCoords(), 0D);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullInterpolation() {
		ResourceProperties.create().interpolation(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToCopyNullProperties() {
		ResourceProperties.create().copy(null);
	}

}