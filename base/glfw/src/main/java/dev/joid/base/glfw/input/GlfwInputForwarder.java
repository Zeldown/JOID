package dev.joid.base.glfw.input;

import org.lwjgl.glfw.GLFW;

import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.utils.click.ClickType;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwInputForwarder {

	private final UIBridge           bridge;
	private final KeyCharacterMerger merger;

	public static @NonNull GlfwInputForwarder create(final @NonNull UIBridge bridge) {
		return new GlfwInputForwarder(bridge, KeyCharacterMerger.create(bridge::keyTyped));
	}

	public @NonNull GlfwInputForwarder attach(final long window) {
		GLFW.glfwSetCharCallback(window, (handle, codepoint) -> this.charTyped(codepoint));
		GLFW.glfwSetCursorPosCallback(window, (handle, x, y) -> this.mouseMoved());
		GLFW.glfwSetScrollCallback(window, (handle, x, y) -> this.mouseScrolled(x, y));
		GLFW.glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
			if (action != GLFW.GLFW_RELEASE) {
				this.keyPressed(key, mods);
			}
		});
		GLFW.glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> {
			if (action == GLFW.GLFW_PRESS) {
				this.mousePressed(button);
			} else {
				this.mouseReleased(button);
			}
		});
		return this;
	}

	public void keyPressed(final int code, final int modifiers) {
		this.merger.keyPressed(GlfwKeys.getKey(code), code, modifiers);
	}

	public void charTyped(final int codepoint) {
		this.merger.charTyped(codepoint);
	}

	public boolean mousePressed(final int button) {
		this.merger.flush();
		return this.bridge.mousePressed(ClickType.from(button));
	}

	public boolean mouseReleased(final int button) {
		return this.bridge.mouseReleased(ClickType.from(button));
	}

	public boolean mouseMoved() {
		return this.bridge.mouseMoved();
	}

	public boolean mouseScrolled(final double notchesX, final double notchesY) {
		return this.bridge.mouseScroll(notchesX, notchesY);
	}

	public void flush() {
		this.merger.flush();
	}

}