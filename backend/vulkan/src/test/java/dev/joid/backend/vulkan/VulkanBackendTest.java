package dev.joid.backend.vulkan;

import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.backend.vulkan.host.IVulkanHost;
import dev.joid.backend.vulkan.host.VulkanWindowHost;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;

public class VulkanBackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), VulkanBackend.create(0L, VulkanBackendTest.host(2)).getVersion());
	}

	@Test
	public void rejectsAHostWithoutFrameInFlight() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> VulkanRenderBridge.create(VulkanBackendTest.host(0)));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("getFramesInFlight() must return 1 or more"));
	}

	@Test
	public void rejectsAWindowHostWithoutFrameInFlight() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> VulkanWindowHost.create(0L, 0));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("at least one frame in flight, got 0"));
	}

	@Test
	public void refusesToDestroyABackendNeverRegistered() {
		final IllegalStateException exception = Assert.assertThrows(IllegalStateException.class, () -> VulkanBackend.create(0L, VulkanBackendTest.host(2)).destroy());
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("call register() first"));
	}

	private static IVulkanHost host(final int framesInFlight) {
		return (IVulkanHost) Proxy.newProxyInstance(IVulkanHost.class.getClassLoader(), new Class<?>[] {IVulkanHost.class}, (proxy, method, arguments) -> "getFramesInFlight".equals(method.getName()) ? Integer.valueOf(framesInFlight) : null);
	}

}