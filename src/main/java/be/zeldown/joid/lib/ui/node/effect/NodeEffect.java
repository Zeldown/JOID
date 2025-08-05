package be.zeldown.joid.lib.ui.node.effect;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeEffect<T extends Node> {

	private int priority;

	public void init(final @NonNull T node, final @NonNull UI ui) {}
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {}
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {}

	public boolean shouldApply(final @NonNull T node) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public @NonNull <E extends NodeEffect<T>> E priority(final int priority) {
		this.priority = priority;
		return (E) this;
	}

}