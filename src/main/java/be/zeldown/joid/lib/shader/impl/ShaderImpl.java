package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

public abstract class ShaderImpl {

	@Getter
	protected IShader shader;

	protected void load(final @NonNull InputStream vertexShader, final @NonNull InputStream fragmentShader) {
		try {
			this.shader = BridgeHandler.RENDER.get().createShader(vertexShader, fragmentShader, BlendState.NORMAL);
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