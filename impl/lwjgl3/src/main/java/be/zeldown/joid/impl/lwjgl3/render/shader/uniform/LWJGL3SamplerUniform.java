package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;

import be.zeldown.joid.impl.lwjgl3.render.LWJGL3RenderBridge;
import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.impl.lwjgl3.render.texture.LWJGL3Texture;
import be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class LWJGL3SamplerUniform extends LWJGL3ShaderUniform implements SamplerUniform {

	private final int unit;

	private LWJGL3Texture texture;
	private TextureFilter filter;
	private TextureWrap   wrap;

	public LWJGL3SamplerUniform(final LWJGL3Shader shader, final int location, final int unit) {
		super(shader, location);
		this.unit = unit;
	}

	@Override
	public void setValue(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.texture = (LWJGL3Texture) texture;
		this.filter  = filter;
		this.wrap    = wrap;
	}

	public void apply(final LWJGL3RenderBridge bridge) {
		if (this.texture == null || super.getLocation() == -1) {
			return;
		}

		GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + this.unit);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.texture.getId());
		GL33C.glBindSampler(this.unit, bridge.getSampler(this.filter, this.wrap));
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL20C.glUniform1i(super.getLocation(), this.unit);
	}

}