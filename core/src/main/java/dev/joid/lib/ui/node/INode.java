package dev.joid.lib.ui.node;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.list.IIndexedElement;
import lombok.NonNull;

public interface INode extends IIndexedElement {

	public default void init(final @NonNull UI ui) {}
	public default void update() {}
	public default void layout() {}

	public default void draw(final double mouseX, final double mouseY) {}
	public default void drawSkeleton(final double mouseX, final double mouseY) {}

	public default void mousePressed(final @NonNull NodeMouseEvent<?> event) {}
	public default void mouseReleased(final @NonNull NodeMouseEvent<?> event) {}
	public default void mouseDragged(final @NonNull NodeMouseEvent<?> event) {}
	public default void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {}

	public default void charTyped(final @NonNull NodeCharEvent<?> event) {}
	public default void keyPressed(final @NonNull NodeKeyEvent<?> event) {}
	public default void keyReleased(final @NonNull NodeKeyEvent<?> event) {}

	public default void detach() {}

}