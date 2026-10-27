package com.example.backend.engine;

public interface EngineListener {

	public default boolean keyPressed(final int code) {
		return false;
	}

	public default boolean keyRepeated(final int code) {
		return false;
	}

	public default boolean keyReleased(final int code) {
		return false;
	}

	public default boolean charTyped(final int codepoint) {
		return false;
	}

	public default boolean mouseMoved() {
		return false;
	}

	public default boolean mousePressed(final int button) {
		return false;
	}

	public default boolean mouseReleased(final int button) {
		return false;
	}

	public default boolean mouseScrolled(final double x, final double y) {
		return false;
	}

}