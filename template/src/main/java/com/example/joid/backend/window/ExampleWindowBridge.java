package com.example.joid.backend.window;

import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.key.Key;

public final class ExampleWindowBridge implements IWindowBridge {

	@Override
	public int getWidth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMouseX() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMouseY() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getClipboard() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMouseGrabbed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isKeyDown(final Key key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClipboard(final String text) {
		throw new UnsupportedOperationException();
	}

}