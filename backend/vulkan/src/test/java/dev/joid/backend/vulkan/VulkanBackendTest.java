package dev.joid.backend.vulkan;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.vulkan.VKCapabilitiesDevice;
import org.lwjgl.vulkan.VkDevice;

import dev.joid.backend.vulkan.host.IVulkanHost;
import dev.joid.backend.vulkan.host.WindowVulkanHost;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;

public class VulkanBackendTest {

	@Test
	public void registersWithTheVersionOfTheJOIDItEmbeds() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), VulkanBackend.create(0L, VulkanBackendTest.host(2, null)).getVersion());
	}

	@Test
	public void rejectsAHostWithoutFrameInFlight() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> new VulkanRenderBridge(VulkanBackendTest.host(0, null)));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("getFramesInFlight() must return 1 or more"));
	}

	@Test
	public void rejectsAHostWhoseDeviceIsNotVulkan13() throws ReflectiveOperationException {
		final VkDevice device = VulkanBackendTest.allocate(VkDevice.class);
		final Field capabilities = VkDevice.class.getSuperclass().getDeclaredField("capabilities");
		capabilities.setAccessible(true);
		capabilities.set(device, VulkanBackendTest.allocate(VKCapabilitiesDevice.class));
		final IllegalStateException exception = Assert.assertThrows(IllegalStateException.class, () -> new VulkanRenderBridge(VulkanBackendTest.host(2, device)));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("gives a device without Vulkan 1.3"));
	}

	@Test
	public void rejectsAWindowHostWithoutFrameInFlight() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> WindowVulkanHost.create(0L, 0));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("at least one frame in flight, got 0"));
	}

	@Test
	public void refusesToDestroyABackendNeverRegistered() {
		final IllegalStateException exception = Assert.assertThrows(IllegalStateException.class, () -> VulkanBackend.create(0L, VulkanBackendTest.host(2, null)).destroy());
		Assert.assertTrue(exception.getMessage(), exception.getMessage().contains("call register() first"));
	}

	private static IVulkanHost host(final int framesInFlight, final VkDevice device) {
		return (IVulkanHost) Proxy.newProxyInstance(IVulkanHost.class.getClassLoader(), new Class<?>[] {IVulkanHost.class}, (proxy, method, arguments) -> "getFramesInFlight".equals(method.getName()) ? Integer.valueOf(framesInFlight) : "getDevice".equals(method.getName()) ? device : null);
	}

	private static <T> T allocate(final Class<T> type) throws ReflectiveOperationException {
		final Field field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
		field.setAccessible(true);
		final Object unsafe = field.get(null);
		return type.cast(unsafe.getClass().getMethod("allocateInstance", Class.class).invoke(unsafe, type));
	}

}