package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanUniformBlock {

	private final int                              binding;
	private final ByteBuffer                       data;
	private final Map<String, VulkanUniformMember> memberMap;

	public static @NonNull VulkanUniformBlock create(final int binding, final int size) {
		return new VulkanUniformBlock(binding, ByteBuffer.allocateDirect(Math.max(size, 4)).order(ByteOrder.nativeOrder()), new HashMap<>());
	}

}