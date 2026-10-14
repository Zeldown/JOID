package dev.joid.backend.vulkan;

import org.junit.Assert;
import org.junit.Test;

public class VulkanBackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), VulkanBackend.create(0L).getVersion());
	}

}