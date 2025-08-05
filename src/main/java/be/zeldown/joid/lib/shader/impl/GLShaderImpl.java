package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import lombok.Getter;
import lombok.NonNull;

public abstract class GLShaderImpl {

	@Getter
	protected IGLShader shader;

	protected void load(final @NonNull InputStream vertexShader, final @NonNull InputStream fragmentShader) {
		try {
			this.shader = GLShader.from(vertexShader, fragmentShader, ShaderBlendState.NORMAL);
		} catch (final Exception e) {
			System.err.println("Erreur lors du chargement du shader " + vertexShader + "/" + fragmentShader + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void bind() {
		if (this.shader != null && this.shader.isActive()) {
			this.shader.bind();
		}
	}

	public void unbind() {
		if (this.shader != null) {
			this.shader.unbind();
		}
	}

	public boolean isAvailable() {
		return this.shader != null && this.shader.isActive();
	}

}