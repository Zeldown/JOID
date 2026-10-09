package dev.joid.backend.vulkan.render.texture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanImage {

	private final long image;
	private final long view;
	private final int  width;
	private final int  height;
	private final int  levels;

	public static @NonNull VulkanImage create(final long image, final long view, final int width, final int height, final int levels) {
		return new VulkanImage(image, view, width, height, levels);
	}

}