package dev.joid.lib.ui.core;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.node.callback.DispatchContext;
import lombok.NonNull;

public interface IUI {

	default public void init() {}

	default public boolean close() {
		return true;
	}

	default public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {}

	default public void mouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull DispatchContext context) {}

	default public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {}

	default public void mouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull DispatchContext context) {}

	default public void keyPressed(final char c, final @NonNull Key key, final @NonNull DispatchContext context) {}

	default public void drawBackground(final double mouseX, final double mouseY) {}

	default public void preDraw(final double mouseX, final double mouseY) {}

	default public void postDraw(final double mouseX, final double mouseY) {}

	default public void update() {}

}