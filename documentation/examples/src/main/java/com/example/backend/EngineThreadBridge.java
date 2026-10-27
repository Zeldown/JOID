package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.lib.bridge.thread.IThreadBridge;

// #region thread
public final class EngineThreadBridge implements IThreadBridge {

	private final Engine engine;

	public EngineThreadBridge(final Engine engine) {
		this.engine = engine;
	}

	@Override
	public boolean isRenderThread() {
		return this.engine.isRenderThread();
	}

	@Override
	public void execute(final Runnable runnable) {
		this.engine.post(runnable);
	}

}
// #endregion