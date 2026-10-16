package dev.joid.lib.ui.node.impl.control.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

public abstract class SelectorNode<V> extends BaseSelectorNode<V> {

	private final List<Node> optionList;

	@Getter private Node              field;
	@Getter private SelectorDirection direction;

	private Function<V, Node> option;

	private double raise;
	private double raisedLayoutY;

	protected SelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionList = new ArrayList<>();
		this.direction  = SelectorDirection.DOWN;

		super.<SelectorNode<V>>require("field", "give it one with field(...)", node -> node.field != null);
		super.<SelectorNode<V>>require("option", "give it one with option(...)", node -> node.option != null);
	}

	@Override
	public void update() {
		super.layoutSize(super.getWidth(), this.getShownHeight());
	}

	@Override
	public void layout() {
		final double size = super.getDefaultHeight();
		final double height = this.getShownHeight();
		final double raise = this.direction.isDown() ? 0D : height - size;
		final double layoutY = super.getLayoutY() == this.raisedLayoutY ? super.getLayoutY() + this.raise : super.getLayoutY();
		this.raise = raise;
		this.raisedLayoutY = layoutY - raise;
		super.layoutPosition(super.getLayoutX(), this.raisedLayoutY).layoutSize(super.getWidth(), height);
		this.field.layoutPosition(0D, 0D).layoutSize(super.getWidth(), height);

		final List<V> values = super.getValueList();
		for (int i = 0; i < this.optionList.size(); i++) {
			final Node option = this.optionList.get(i);
			final double rank = super.getOptionRank(values.get(i));
			option.visible(super.isActive() || this.isSelected(option)).layoutPosition(0D, this.direction.isDown() ? rank * size : raise - rank * size).layoutSize(super.getWidth(), size);
		}
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.getButton().isLeft() && super.isActive() && !event.isConsumed()) {
			final Node clicked = this.optionList.stream().filter(Node::isHovered).findFirst().orElse(null);
			if (clicked != null && clicked != this.getSelected()) {
				super.change(super.getValueList().get(this.optionList.indexOf(clicked)));
			}
		}

		super.mousePressed(event);
	}

	@Override
	protected void createOptions() {
		this.optionList.forEach(option -> super.remove(option));
		this.optionList.clear();
		if (this.option == null) {
			return;
		}

		for (final V value : super.getValueList()) {
			final Node option = this.option.apply(value);
			this.optionList.add(option);
			super.append(option.visible(super.isActive() || this.isSelected(option)));
		}
	}

	public final boolean isSelected(final @NonNull Node option) {
		return this.getSelected() == option;
	}

	public final Node getSelected() {
		final int index = super.getValueIndex();
		return index >= 0 && index < this.optionList.size() ? this.optionList.get(index) : null;
	}

	public final @NonNull List<@NonNull Node> getOptionList() {
		return Collections.unmodifiableList(this.optionList);
	}

	public final <T extends SelectorNode<V>> @NonNull T field(final @NonNull Node field) {
		this.field = super.part(this.field, field.zindex(-1));
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T option(final @NonNull Function<V, Node> option) {
		this.option = option;
		this.createOptions();
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull SelectorDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull Supplier<@NonNull SelectorDirection> direction) {
		return super.follow("direction", direction, value -> this.direction = value);
	}

	private double getShownHeight() {
		return super.isActive() ? super.getDefaultHeight() * this.optionList.size() : super.getDefaultHeight();
	}

	public static enum SelectorDirection {

		UP,
		DOWN;

		public final boolean isDown() {
			return this == SelectorDirection.DOWN;
		}

	}

}