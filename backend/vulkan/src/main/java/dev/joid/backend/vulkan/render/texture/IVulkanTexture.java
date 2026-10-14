package dev.joid.backend.vulkan.render.texture;

import dev.joid.lib.bridge.render.texture.ITexture;

public interface IVulkanTexture extends ITexture {

	public long getView();

}