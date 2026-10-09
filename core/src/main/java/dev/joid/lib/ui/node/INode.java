package dev.joid.lib.ui.node;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.utils.list.RecursiveIndexedElement;
import lombok.NonNull;

public interface INode extends RecursiveIndexedElement, Cloneable {

	default public void update() {}
	default public void detach() {}
	default public void init(final @NonNull UI ui) {}

	default public void draw(final double mouseX, final double mouseY) {}
	default public void drawSkeleton(final double mouseX, final double mouseY) {}

	default public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {}
	default public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {}
	default public void mouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull DispatchContext context) {}
	default public void mouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull DispatchContext context) {}

	default public void charTyped(final int codepoint, final @NonNull DispatchContext context) {}
	default public void keyPressed(final @NonNull Key key, final @NonNull DispatchContext context) {}

}