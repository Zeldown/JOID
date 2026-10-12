package dev.joid.backend.vulkan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;

public class VulkanBackendTest {

	@Test
	public void keepsTheVersionOfJOIDItWasBuiltAgainst() throws IOException {
		final String bytecode = new String(Asset.of(VulkanBackend.class.getResourceAsStream("VulkanBackend.class")).read(), StandardCharsets.ISO_8859_1);
		Assert.assertTrue(bytecode.contains(JOID.VERSION));
	}

}