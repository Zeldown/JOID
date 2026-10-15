package dev.joid.lib.ui.node;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.list.IndexedElement;
import lombok.NonNull;

public interface INode extends IndexedElement {

	default public void update() {}
	default public void detach() {}
	default public void init(final @NonNull UI ui) {}

	default public void draw(final double mouseX, final double mouseY) {}
	default public void drawSkeleton(final double mouseX, final double mouseY) {}

	default public void mousePressed(final @NonNull NodeMouseEvent<?> event) {}
	default public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {}
	default public void mouseDragged(final @NonNull NodeMouseEvent<?> event) {}
	default public void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {}

	default public void charTyped(final @NonNull NodeCharEvent<?> event) {}
	default public void keyPressed(final @NonNull NodeKeyEvent<?> event) {}
	default public void keyReleased(final @NonNull NodeKeyEvent<?> event) {}

}