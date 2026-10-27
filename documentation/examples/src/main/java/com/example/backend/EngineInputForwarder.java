package com.example.backend;

import com.example.backend.engine.Engine;
import com.example.backend.engine.EngineListener;

import dev.joid.base.glfw.input.GlfwKeys;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyLayout;
import dev.joid.lib.input.mouse.MouseButton;

// #region forwarder
public final class EngineInputForwarder implements EngineListener {

	private final KeyLayout layout;

	public EngineInputForwarder(final Engine engine) {
		this.layout = KeyLayout.from(key -> engine.getKeyName(GlfwKeys.getCode(key)));
	}

	@Override
	public boolean keyPressed(final int code) {
		final Key physicalKey = GlfwKeys.getPhysicalKey(code);
		return BridgeHandler.UI.keyPressed(physicalKey, this.layout.translate(physicalKey));
	}

	@Override
	public boolean keyRepeated(final int code) {
		return BridgeHandler.UI.isKeyConsumed(GlfwKeys.getPhysicalKey(code));
	}

	@Override
	public boolean keyReleased(final int code) {
		return BridgeHandler.UI.keyReleased(GlfwKeys.getPhysicalKey(code));
	}

	@Override
	public boolean charTyped(final int codepoint) {
		return BridgeHandler.UI.charTyped(codepoint);
	}

	@Override
	public boolean mouseMoved() {
		return BridgeHandler.UI.mouseMoved();
	}

	@Override
	public boolean mousePressed(final int button) {
		return BridgeHandler.UI.mousePressed(MouseButton.of(button));
	}

	@Override
	public boolean mouseReleased(final int button) {
		return BridgeHandler.UI.mouseReleased(MouseButton.of(button));
	}

	@Override
	public boolean mouseScrolled(final double x, final double y) {
		return BridgeHandler.UI.mouseScroll(x, y);
	}

}
// #endregion