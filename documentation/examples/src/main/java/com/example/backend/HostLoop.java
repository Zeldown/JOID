package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.lib.bridge.backend.Backend;

public final class HostLoop {

	private final Engine  engine;
	private final Backend backend;

	public HostLoop(final Engine engine, final IGlBinding binding) {
		this.engine  = engine;
		this.backend = GlHostBackend.create(engine, binding).register();
	}

	// #region loop
	public void run() {
		while (this.engine.isRunning()) {
			this.engine.pollEvents();
			this.engine.drawScene();
			this.backend.frame();
			this.engine.present();
		}
	}
	// #endregion

}