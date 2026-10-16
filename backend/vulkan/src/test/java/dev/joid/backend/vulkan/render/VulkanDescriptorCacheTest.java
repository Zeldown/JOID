package dev.joid.backend.vulkan.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import dev.joid.backend.vulkan.render.descriptor.VulkanDescriptorCache;
import dev.joid.backend.vulkan.render.texture.VulkanImage;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.resource.Resource;
import dev.joid.test.contract.ContractDraw;

public class VulkanDescriptorCacheTest {

	private final VulkanBorrowedTextureBackend backend = new VulkanBorrowedTextureBackend();

	@Before
	public void startBackend() {
		this.backend.create(64, 64);
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().identity();
		render.getProjection().ortho(0D, 64D, 64D, 0D, 0D, 10000D);
		render.getState().load(RenderState.create().viewport(0, 0, 64, 64).blend(BlendState.NORMAL).depthWrite(false));
	}

	@After
	public void stopBackend() {
		this.backend.destroy();
	}

	@Test
	public void releasesTheSetsOfAViewNoLongerBorrowed() throws ReflectiveOperationException {
		final VulkanImage red = (VulkanImage) this.backend.createBorrowableTexture(4, 4, 0xFFFF0000, false);
		final VulkanImage blue = (VulkanImage) this.backend.createBorrowableTexture(4, 4, 0xFF0000FF, false);
		final AtomicReference<Object> current = new AtomicReference<>(red);
		final Resource resource = Resource.of(this.backend.borrow(current::get));
		this.render(resource);
		Assert.assertTrue(VulkanDescriptorCacheTest.isCached(red.getView()));
		current.set(blue);
		this.render(resource);
		Assert.assertFalse(VulkanDescriptorCacheTest.isCached(red.getView()));
		Assert.assertTrue(VulkanDescriptorCacheTest.isCached(blue.getView()));
	}

	@Test
	public void allocatesInTheRoomFreedInAnOlderPool() throws ReflectiveOperationException {
		final VulkanContext context = BridgeHandler.RENDER.getBridge(VulkanRenderBridge.class).getContext();
		final VkDevice device = context.getDevice();
		final VulkanDescriptorCache cache = VulkanDescriptorCache.create(context);
		final long layout = VulkanDescriptorCacheTest.createLayout(device);
		final Method allocate = VulkanDescriptorCache.class.getDeclaredMethod("allocate", long.class);
		allocate.setAccessible(true);
		final Field pools = VulkanDescriptorCache.class.getDeclaredField("poolList");
		pools.setAccessible(true);
		try {
			final long[] first = (long[]) allocate.invoke(cache, layout);
			for (int index = 1; index < 1024; index++) {
				allocate.invoke(cache, layout);
			}

			allocate.invoke(cache, layout);
			Assert.assertEquals(2, ((List<?>) pools.get(cache)).size());
			VK10.vkFreeDescriptorSets(device, first[1], first[0]);
			for (int index = 1; index < 1024; index++) {
				allocate.invoke(cache, layout);
			}

			Assert.assertEquals(first[1], ((long[]) allocate.invoke(cache, layout))[1]);
			Assert.assertEquals(2, ((List<?>) pools.get(cache)).size());
		} finally {
			cache.destroy();
			VK10.vkDestroyDescriptorSetLayout(device, layout, null);
		}
	}

	private void render(final Resource resource) {
		ContractDraw.frame(this.backend, 64, 64, render -> resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> render.draw(Primitive.TRIANGLES, ContractDraw.quad(0F, 0F, 64F, 64F, 0F, true, 0, false, 0F, 1F))));
	}

	private static boolean isCached(final long view) throws ReflectiveOperationException {
		final VulkanDescriptorCache cache = BridgeHandler.RENDER.getBridge(VulkanRenderBridge.class).getDescriptorCache();
		final Field sets = VulkanDescriptorCache.class.getDeclaredField("setMap");
		sets.setAccessible(true);
		return ((Map<List<Long>, ?>) sets.get(cache)).keySet().stream().anyMatch(key -> key.contains(view));
	}

	private static long createLayout(final VkDevice device) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(1, stack);
			bindings.get(0).binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
			final LongBuffer layout = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateDescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings), null, layout), "vkCreateDescriptorSetLayout");
			return layout.get(0);
		}
	}

}