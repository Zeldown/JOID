package com.example.joid.backend.window;

import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;

public final class ExampleWindowBridge implements IWindowBridge {

	@Override
	public int getWidth() {
		throw new UnsupportedOperationException("ExampleWindowBridge.getWidth() is not implemented, implement it with the API of your engine");
	}

	@Override
	public int getHeight() {
		throw new UnsupportedOperationException("ExampleWindowBridge.getHeight() is not implemented, implement it with the API of your engine");
	}

	@Override
	public double getMouseX() {
		throw new UnsupportedOperationException("ExampleWindowBridge.getMouseX() is not implemented, implement it with the API of your engine");
	}

	@Override
	public double getMouseY() {
		throw new UnsupportedOperationException("ExampleWindowBridge.getMouseY() is not implemented, implement it with the API of your engine");
	}

	@Override
	public boolean isMouseGrabbed() {
		throw new UnsupportedOperationException("ExampleWindowBridge.isMouseGrabbed() is not implemented, implement it with the API of your engine");
	}

	@Override
	public boolean isKeyDown(final Key physicalKey) {
		throw new UnsupportedOperationException("ExampleWindowBridge.isKeyDown() is not implemented, implement it with the API of your engine");
	}

	@Override
	public String getClipboard() {
		throw new UnsupportedOperationException("ExampleWindowBridge.getClipboard() is not implemented, implement it with the API of your engine");
	}

	@Override
	public void setClipboard(final String text) {
		throw new UnsupportedOperationException("ExampleWindowBridge.setClipboard() is not implemented, implement it with the API of your engine");
	}

}