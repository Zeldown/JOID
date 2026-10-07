package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.RenderBridge;
import dev.joid.impl.lwjgl2.render.shader.Shader;
import dev.joid.impl.lwjgl2.render.texture.Texture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SamplerUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.SamplerUniform {

	private final int unit;

	private Texture       texture;
	private TextureWrap   wrap;
	private int           previousTexture;
	private TextureFilter filter;

	public SamplerUniform(final Shader shader, final int location, final int unit) {
		super(shader, location);
		this.unit = unit;
	}

	@Override
	public void setValue(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		if (super.getShader().isBound()) {
			this.unbind();
		}

		this.texture = (Texture) texture;
		this.filter  = filter;
		this.wrap    = wrap;

		if (super.getShader().isBound()) {
			this.bind();
		}
	}

	public void bind() {
		if (this.texture == null) {
			return;
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0 + this.unit);
		this.previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture.getId());
		RenderBridge.applyTextureParameters(this.filter, this.wrap);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL20.glUniform1i(super.getLocation(), this.unit);
	}

	public void unbind() {
		if (this.texture == null) {
			return;
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0 + this.unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.previousTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}

}