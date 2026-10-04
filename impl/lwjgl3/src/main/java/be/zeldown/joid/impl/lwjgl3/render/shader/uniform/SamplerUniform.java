package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;

import be.zeldown.joid.impl.lwjgl3.render.RenderBridge;
import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;
import be.zeldown.joid.impl.lwjgl3.render.texture.Texture;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SamplerUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform {

	private final int unit;

	private Texture       texture;
	private TextureFilter filter;
	private TextureWrap   wrap;

	public SamplerUniform(final Shader shader, final int location, final int unit) {
		super(shader, location);
		this.unit = unit;
	}

	@Override
	public void setValue(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.texture = (Texture) texture;
		this.filter  = filter;
		this.wrap    = wrap;
	}

	public void apply(final RenderBridge bridge) {
		if (this.texture == null || super.getLocation() == -1) {
			return;
		}

		GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + this.unit);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.texture.getId());
		GL33C.glBindSampler(this.unit, bridge.getSampler(this.filter, this.wrap, this.texture.isMipmapped()));
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL20C.glUniform1i(super.getLocation(), this.unit);
	}

}