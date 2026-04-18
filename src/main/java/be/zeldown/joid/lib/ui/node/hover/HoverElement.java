package be.zeldown.joid.lib.ui.node.hover;

import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

@FunctionalInterface
public interface HoverElement {

	public void render(final @NonNull Node node, final double mouseX, final double mouseY);

	default double getX() { return 0; }
	default double getY() { return 0; }
	default double getWidth() { return 0; }
	default double getHeight() { return 0; }

}