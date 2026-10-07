package dev.joid.lib.shader.impl;

import java.io.InputStream;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

public abstract class ShaderImpl {

	@Getter
	protected IShader shader;

	private boolean warned;

	protected void load(final @NonNull InputStream vertexShader, final @NonNull InputStream fragmentShader) {
		try (final InputStream vertex = vertexShader; final InputStream fragment = fragmentShader) {
			this.shader = BridgeHandler.RENDER.get().createShader(ShaderSource.read(ShaderStage.VERTEX, vertex), ShaderSource.read(ShaderStage.FRAGMENT, fragment), BlendState.NORMAL);
		} catch (final Exception e) {
			System.err.println("[JOID] Unable to load the shader " + this.getClass().getSimpleName() + ": " + e.getMessage());
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

	public boolean canDraw() {
		final boolean available = this.isAvailable();
		if (!available && JOID.inst().isDevMode() && !this.warned) {
			this.warned = true;
			System.err.println("[JOID] The shader " + this.getClass().getSimpleName() + " is unavailable, what it draws is skipped");
		}
		return available;
	}

	public boolean isAvailable() {
		return this.shader != null && this.shader.isActive();
	}

}