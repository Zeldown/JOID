package dev.joid.backend.vulkan;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;

public class VulkanBackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(JOID.VERSION, VulkanBackend.create(0L).getVersion());
	}

}