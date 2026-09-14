package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import be.zeldown.joid.impl.lwjgl2.render.RenderBridge;
import be.zeldown.joid.impl.lwjgl2.render.shader.Shader;
import be.zeldown.joid.impl.lwjgl2.render.texture.Texture;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SamplerUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform {

	private final int    unit;
	private final Shader shader;

	private Texture       texture;
	private TextureFilter filter;
	private TextureWrap   wrap;
	private int           previousTexture;

	public SamplerUniform(final int location, final int unit, final Shader shader) {
		super(location);
		this.unit   = unit;
		this.shader = shader;
	}

	@Override
	public void setValue(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		if (this.shader.isBound()) {
			this.unbind();
		}

		this.texture = (Texture) texture;
		this.filter  = filter;
		this.wrap    = wrap;

		if (this.shader.isBound()) {
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