package dev.joid.lib.shader.impl;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.NonNull;

public abstract class ShaderImpl {

	private IShader      shader;
	private boolean      warned;
	private ShaderSource vertex;
	private ShaderSource fragment;

	protected void load(final @NonNull Object vertexShader, final @NonNull Object fragmentShader) {
		try {
			try {
				this.vertex = ShaderSource.read(ShaderStage.VERTEX, vertexShader);
			} finally {
				this.fragment = ShaderSource.read(ShaderStage.FRAGMENT, fragmentShader);
			}
		} catch (final Exception e) {
			this.vertex = null;
			System.err.println("[JOID] Unable to load the shader " + this.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public IShader getShader() {
		if (this.shader == null && this.vertex != null) {
			try {
				this.shader = BridgeHandler.RENDER.get().createShader(this.vertex, this.fragment, BlendState.NORMAL);
			} catch (final Exception e) {
				this.vertex = null;
				System.err.println("[JOID] Unable to create the shader " + this.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		return this.shader;
	}

	public void bind() {
		final IShader current = this.getShader();
		if (current != null && current.isActive()) {
			current.bind();
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
		final IShader current = this.getShader();
		return current != null && current.isActive();
	}

}