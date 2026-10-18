package dev.joid.demo.asset.locator.impl;

import org.junit.Assert;
import org.junit.Test;

public class DemoAssetLocatorTest {

	@Test
	public void refusesADemoAssetThatDoesNotExist() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> new DemoAssetLocator().locate("demo:textures/absent.png"));
		Assert.assertEquals("The asset demo:textures/absent.png is not in the demo assets, check its path", exception.getMessage());
	}

	@Test
	public void locatesTheSameDemoAssetUnderTheSameId() {
		Assert.assertEquals(new DemoAssetLocator().locate("demo:models/texture.png").getUniqueId(), new DemoAssetLocator().locate("demo:models/texture.png").getUniqueId());
	}

}