package dev.joid.backend.lwjgl3;

import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.lwjgl3.glfw.GlfwWindowBridge;
import dev.joid.base.lwjgl3.glfw.binding.Lwjgl3AlBinding;
import dev.joid.base.lwjgl3.glfw.input.GlfwInputForwarder;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.NonNull;

public final class Lwjgl3Backend extends Backend {

	private final long window;

	private IGlBinding binding;

	private Lwjgl3Backend(final long window) {
		this.window  = window;
		this.binding = Lwjgl3GlBinding.inst();
	}

	public static @NonNull Lwjgl3Backend create(final long window) {
		return new Lwjgl3Backend(window);
	}

	public @NonNull Lwjgl3Backend binding(final @NonNull IGlBinding binding) {
		this.binding = binding;
		return this;
	}

	@Override
	protected @NonNull IWindowBridge createWindowBridge() {
		return new GlfwWindowBridge(this.window);
	}

	@Override
	protected @NonNull IRenderBridge createRenderBridge() {
		return new GlRenderBridge(this.binding);
	}

	@Override
	protected @NonNull IAudioBridge createAudioBridge() {
		return AlAudioBridge.create(Lwjgl3AlBinding.inst());
	}

	@Override
	protected void attachInput() {
		GlfwInputForwarder.attach(this.window);
	}

}