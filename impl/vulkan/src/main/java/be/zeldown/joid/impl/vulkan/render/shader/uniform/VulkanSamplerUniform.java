package be.zeldown.joid.impl.vulkan.render.shader.uniform;

import be.zeldown.joid.impl.vulkan.render.texture.VulkanTexture;
import be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanSamplerUniform implements SamplerUniform {

	private VulkanTexture texture;
	private TextureFilter filter;
	private TextureWrap   wrap;

	@Override
	public void setValue(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.texture = (VulkanTexture) texture;
		this.filter  = filter;
		this.wrap    = wrap;
	}

}