package dev.joid.backend.lwjgl3;

import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.base.glfw.input.GlfwInputForwarder;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.openal.binding.Lwjgl3AlBinding;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.NonNull;

public final class Lwjgl3Backend extends Backend<Lwjgl3Backend> {

	private final long window;

	private IGlBinding binding;

	private Lwjgl3Backend(final long window) {
		super(JOID.VERSION);
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
	protected @NonNull IAudioBridge createAudioBridge() {
		return AlAudioBridge.create(Lwjgl3AlBinding.inst());
	}

	@Override
	protected @NonNull IWindowBridge createWindowBridge() {
		return new GlfwWindowBridge(this.window);
	}

	@Override
	protected @NonNull IRenderBridge createRenderBridge() {
		return GlRenderBridge.create(this.binding);
	}

	@Override
	protected void registerDefaults() {
		BridgeHandler.UI.registerDefault(WindowUIBridge.create());
	}

	@Override
	protected void attachInput() {
		GlfwInputForwarder.create().attach(this.window);
	}

}