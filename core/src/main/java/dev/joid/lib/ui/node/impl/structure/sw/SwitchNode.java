package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.choice.ChoiceNode;
import lombok.NonNull;

public abstract class SwitchNode extends ChoiceNode<String> {

	private final List<Node> optionList;

	private Function<String, Node> option;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionList = new ArrayList<>();

		super.<SwitchNode>require("option", "give it one with option(...)", node -> node.option != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
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
	protected void layoutValues() {
		this.optionList.forEach(option -> super.remove(option));
		this.optionList.clear();
		if (this.option == null) {
			return;
		}

		for (final String value : super.getValueList()) {
			final Node option = this.option.apply(value);
			this.optionList.add(option);
			super.append(option);
		}
	}

	public final @NonNull List<@NonNull Node> getOptionList() {
		return Collections.unmodifiableList(this.optionList);
	}

	public final <T extends SwitchNode> @NonNull T option(final @NonNull Function<String, Node> option) {
		this.option = option;
		this.layoutValues();
		return (T) this;
	}

}