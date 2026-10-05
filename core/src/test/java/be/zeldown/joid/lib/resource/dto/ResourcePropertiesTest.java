package be.zeldown.joid.lib.resource.dto;

import org.junit.Assert;
import org.junit.Test;

public class ResourcePropertiesTest {

	@Test
	public void leavesTheMipmapsToTheDrawByDefault() {
		Assert.assertFalse(ResourceProperties.create().getMipmap().isPresent());
	}

	@Test
	public void keepsAnExplicitMipmapChoice() {
		final ResourceProperties properties = ResourceProperties.create().mipmap(false);
		Assert.assertFalse(properties.getMipmap().get());
		Assert.assertFalse(properties.copy().getMipmap().get());
		Assert.assertTrue(ResourceProperties.create().copy(properties.mipmap(true)).getMipmap().get());
	}

}