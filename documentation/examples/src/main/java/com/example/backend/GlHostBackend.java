package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.base.glfw.binding.Lwjgl3AlBinding;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;

// #region backend
public final class GlHostBackend extends Backend {

	private final Engine     engine;
	private final IGlBinding binding;

	private GlHostBackend(final Engine engine, final IGlBinding binding) {
		this.engine  = engine;
		this.binding = binding;
	}

	public static GlHostBackend create(final Engine engine, final IGlBinding binding) {
		return new GlHostBackend(engine, binding);
	}

	@Override
	protected IWindowBridge createWindowBridge() {
		return new EngineWindowBridge(this.engine);
	}

	@Override
	protected IRenderBridge createRenderBridge() {
		return new GlRenderBridge(this.binding);
	}

	@Override
	protected IAudioBridge createAudioBridge() {
		return AlAudioBridge.create(Lwjgl3AlBinding.inst()).ownContext(false);
	}

	@Override
	protected void attachInput() {
		this.engine.addListener(new EngineInputForwarder(this.engine));
	}

}
// #endregion