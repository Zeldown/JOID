package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.base.lwjgl3.glfw.input.GlfwKeys;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;

// #region window
public final class EngineWindowBridge implements IWindowBridge {

	private final Engine engine;

	public EngineWindowBridge(final Engine engine) {
		this.engine = engine;
	}

	@Override
	public int getWidth() {
		return this.engine.getWidth();
	}

	@Override
	public int getHeight() {
		return this.engine.getHeight();
	}

	@Override
	public double getMouseX() {
		return this.engine.getMouseX();
	}

	@Override
	public double getMouseY() {
		return this.engine.getMouseY();
	}

	@Override
	public boolean isMouseGrabbed() {
		return this.engine.isCursorLocked();
	}

	@Override
	public boolean isKeyDown(final Key physicalKey) {
		return GlfwKeys.isKeyDown(physicalKey, this.engine::isKeyDown);
	}

	@Override
	public String getClipboard() {
		final String text = this.engine.getClipboard();
		return text == null ? "" : text;
	}

	@Override
	public void setClipboard(final String text) {
		this.engine.setClipboard(text);
	}

}
// #endregion