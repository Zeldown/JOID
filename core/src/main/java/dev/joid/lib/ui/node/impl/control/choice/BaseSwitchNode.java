package dev.joid.lib.ui.node.impl.control.choice;

import java.util.List;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import lombok.NonNull;

public abstract class BaseSwitchNode<V> extends BaseChoiceNode<V> {

	protected BaseSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		final List<V> values = super.getValueList();
		final int index = (int) ((event.getMouseX() - super.getAbsoluteX()) / super.getWidth() * values.size());
		super.change(values.get(Math.max(0, Math.min(values.size() - 1, index))));
		event.consume();
	}

}