package dev.joid.lib.ui.node.hover;

import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

@FunctionalInterface
public interface IHoverElement {

	public void render(final @NonNull Node node, final double mouseX, final double mouseY);

	public default double getX() { return 0; }

	public default double getY() { return 0; }

	public default double getWidth() { return 0; }

	public default double getHeight() { return 0; }

}