package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.base.glfw.binding.Lwjgl3AlBinding;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.resource.resolver.ResourceResolver;

// #region backend
public final class EngineBackend extends Backend {

	private final Engine engine;

	private EngineBackend(final Engine engine) {
		this.engine = engine;
	}

	public static EngineBackend create(final Engine engine) {
		return new EngineBackend(engine);
	}

	@Override
	protected IWindowBridge createWindowBridge() {
		return new EngineWindowBridge(this.engine);
	}

	@Override
	protected IRenderBridge createRenderBridge() {
		return new EngineRenderBridge(this.engine.getDevice());
	}

	// #region audio
	@Override
	protected IAudioBridge createAudioBridge() {
		return AlAudioBridge
		.create(Lwjgl3AlBinding.inst())
		.ownContext(false)
		.gain((gain, group) -> gain * this.engine.getVolume(group));
	}
	// #endregion

	@Override
	protected IThreadBridge createThreadBridge() {
		return new EngineThreadBridge(this.engine);
	}

	@Override
	protected void registerDefaults() {
		super.registerDefaults();
		ResourceResolver.register(new EngineSpriteResourceResolver(this.engine.getDevice()));
	}

	@Override
	protected void attachInput() {
		this.engine.addListener(new EngineInputForwarder(this.engine));
	}

}
// #endregion