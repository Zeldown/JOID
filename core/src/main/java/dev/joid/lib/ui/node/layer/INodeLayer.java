package dev.joid.lib.ui.node.layer;

import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

@FunctionalInterface
public interface INodeLayer {

	public void draw(final @NonNull Node node, final double mouseX, final double mouseY);

}