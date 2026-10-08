package dev.joid.impl.vulkan.render.descriptor;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import dev.joid.impl.vulkan.render.Context;
import dev.joid.impl.vulkan.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import lombok.NonNull;

public final class DescriptorCache {

	private final Context                 context;
	private final List<Long>              poolList;
	private final Map<List<Long>, long[]> setMap;

	public DescriptorCache(final Context context) {
		this.context  = context;
		this.poolList = new ArrayList<>();
		this.setMap   = new HashMap<>();
	}

	public long get(final @NonNull Shader shader, final long uniformBuffer, final @NonNull long[] images) {
		final List<Long> key = new ArrayList<>(images.length + 2);
		key.add(shader.getDescriptorSetLayout());
		key.add(uniformBuffer);
		for (final long image : images) {
			key.add(image);
		}

		final long[] cached = this.setMap.get(key);
		if (cached != null) {
			return cached[0];
		}

		final long[] set = this.allocate(shader.getDescriptorSetLayout());
		this.write(set[0], shader, uniformBuffer, images);
		this.setMap.put(key, set);
		return set[0];
	}

	public void invalidate(final long handle, final @NonNull Consumer<Runnable> disposer) {
		final Iterator<Map.Entry<List<Long>, long[]>> iterator = this.setMap.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<List<Long>, long[]> entry = iterator.next();
			if (!entry.getKey().subList(1, entry.getKey().size()).contains(handle)) {
				continue;
			}

			final long[] set = entry.getValue();
			iterator.remove();
			disposer.accept(() -> VK10.vkFreeDescriptorSets(this.context.getDevice(), set[1], set[0]));
		}
	}

	private long[] allocate(final long layout) {
		if (this.poolList.isEmpty()) {
			this.poolList.add(this.createPool());
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer set = stack.mallocLong(1);
			long pool = this.poolList.get(this.poolList.size() - 1);
			int result = VK10.vkAllocateDescriptorSets(this.context.getDevice(), VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(layout)), set);
			if (result == VK11.VK_ERROR_OUT_OF_POOL_MEMORY || result == VK10.VK_ERROR_FRAGMENTED_POOL) {
				pool = this.createPool();
				this.poolList.add(pool);
				result = VK10.vkAllocateDescriptorSets(this.context.getDevice(), VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(stack.longs(layout)), set);
			}

			Context.check(result, "vkAllocateDescriptorSets");
			return new long[] {set.get(0), pool};
		}
	}

	private long createPool() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.calloc(2, stack);
			sizes.get(0).type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(4096);
			sizes.get(1).type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(4096);

			final LongBuffer pool = stack.mallocLong(1);
			Context.check(VK10.vkCreateDescriptorPool(this.context.getDevice(), VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT).maxSets(1024).pPoolSizes(sizes), null, pool), "vkCreateDescriptorPool");
			return pool.get(0);
		}
	}

	private void write(final long set, final Shader shader, final long uniformBuffer, final long[] images) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(shader.getSamplerMap().size() + 1, stack);
			writes.get()
					.sType$Default()
					.dstSet(set)
					.dstBinding(0)
					.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
					.pBufferInfo(VkDescriptorBufferInfo.calloc(1, stack).buffer(uniformBuffer).offset(0L).range(shader.getBlock().getSize()))
					.descriptorCount(1);

			int index = 0;
			for (final UniformSampler sampler : shader.getSamplerMap().values()) {
				writes.get()
						.sType$Default()
						.dstSet(set)
						.dstBinding(sampler.getUnit())
						.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
						.pImageInfo(VkDescriptorImageInfo.calloc(1, stack).imageView(images[index]).sampler(images[index + 1]).imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL))
						.descriptorCount(1);
				index += 2;
			}

			writes.flip();
			VK10.vkUpdateDescriptorSets(this.context.getDevice(), writes, null);
		}
	}

}