package dev.joid.lib.ui.node.impl.control.choice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public abstract class SwitchNode<V> extends BaseChoiceNode<V> {

	private final List<Node> optionList;

	private BiFunction<V, Integer, Node> option;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionList = new ArrayList<>();

		super.<SwitchNode<V>>require("option", "give it one with option(...)", node -> node.option != null);
	}

	@Override
	public void layout() {
		final double width = super.getWidth() / this.optionList.size();
		for (int i = 0; i < this.optionList.size(); i++) {
			this.optionList.get(i).layoutPosition(width * i, 0D).layoutSize(width, super.getHeight());
		}
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft()) {
			return;
		}

		for (int i = 0; i < this.optionList.size(); i++) {
			if (this.optionList.get(i).isHovered()) {
				super.change(super.getValueList().get(i));
				event.consume();
				return;
			}
		}
	}

	@Override
	protected void createOptions() {
		this.optionList.forEach(option -> super.remove(option));
		this.optionList.clear();
		if (this.option == null) {
			return;
		}

		final List<V> values = super.getValueList();
		for (int i = 0; i < values.size(); i++) {
			final Node option = this.option.apply(values.get(i), i);
			this.optionList.add(option);
			super.append(option);
		}
	}

	public final @NonNull List<@NonNull Node> getOptionList() {
		return Collections.unmodifiableList(this.optionList);
	}

	public final <T extends SwitchNode<V>> @NonNull T option(final @NonNull BiFunction<@NonNull V, @NonNull Integer, @NonNull Node> option) {
		this.option = option;
		this.createOptions();
		return (T) this;
	}

}