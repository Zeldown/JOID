package dev.joid.backend.vulkan.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
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
		final List<Long> poolList = (List<Long>) pools.get(cache);
		final long older = VulkanDescriptorCacheTest.createPool(device);
		final long newer = VulkanDescriptorCacheTest.createPool(device);
		poolList.add(older);
		poolList.add(newer);
		try {
			VK10.vkFreeDescriptorSets(device, older, VulkanDescriptorCacheTest.allocate(device, older, layout));
			VulkanDescriptorCacheTest.allocate(device, newer, layout);
			Assume.assumeTrue("The driver fills a descriptor pool beyond its maxSets, no pool is ever full", VulkanDescriptorCacheTest.isFull(device, layout));
			Assert.assertEquals(older, ((long[]) allocate.invoke(cache, layout))[1]);
			Assert.assertEquals(2, poolList.size());
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

	private static long createPool(final VkDevice device) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(1, stack);
			sizes.get(0).type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1);
			final LongBuffer pool = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateDescriptorPool(device, VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT).maxSets(1).pPoolSizes(sizes), null, pool), "vkCreateDescriptorPool");
			return pool.get(0);
		}
	}

	private static long allocate(final VkDevice device, final long pool, final long layout) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer set = stack.mallocLong(1);
			VulkanContext.check(VK10.vkAllocateDescriptorSets(device, VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(layout)), set), "vkAllocateDescriptorSets");
			return set.get(0);
		}
	}

	private static boolean isFull(final VkDevice device, final long layout) {
		final long pool = VulkanDescriptorCacheTest.createPool(device);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			VulkanDescriptorCacheTest.allocate(device, pool, layout);
			return VK10.vkAllocateDescriptorSets(device, VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(layout)), stack.mallocLong(1)) != VK10.VK_SUCCESS;
		} finally {
			VK10.vkDestroyDescriptorPool(device, pool, null);
		}
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