package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.choice.ChoiceNode;
import lombok.Getter;
import lombok.NonNull;

public abstract class SelectorNode<V> extends ChoiceNode<V> {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> OPEN         = EventType.create("onOpen");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CLOSE        = EventType.create("onClose");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_OPEN  = EventType.create("beforeOpen");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CLOSE = EventType.create("beforeClose");

	private final transient InteractiveValue<Boolean> activeValue;

	private final List<Node> optionList;

	@Getter private Node              field;
	@Getter private boolean           active;
	@Getter private SelectorDirection direction;

	private Function<V, Node> option;

	private double raise;
	private double raisedLayoutY;

	protected SelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionList  = new ArrayList<>();
		this.direction   = SelectorDirection.DOWN;
		this.activeValue = InteractiveValue.toggle(super.getBindings(), "active", () -> this.active, active -> this.active = active)
		.events(true, SelectorNode.BEFORE_OPEN, (previous, active) -> NodeChangeEvent.Pre.create(this, previous, active), SelectorNode.OPEN, (previous, active) -> NodeChangeEvent.Post.create(this, previous, active))
		.events(false, SelectorNode.BEFORE_CLOSE, (previous, active) -> NodeChangeEvent.Pre.create(this, previous, active), SelectorNode.CLOSE, (previous, active) -> NodeChangeEvent.Post.create(this, previous, active));

		super.<SelectorNode<V>>require("field", "give it one with field(...)", node -> node.field != null);
		super.<SelectorNode<V>>require("option", "give it one with option(...)", node -> node.option != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double size = super.getDefaultHeight();
		final double height = this.active ? size * this.optionList.size() : size;
		final double raise = this.direction.isDown() ? 0D : height - size;
		final double layoutY = super.getLayoutY() == this.raisedLayoutY ? super.getLayoutY() + this.raise : super.getLayoutY();
		this.raise = raise;
		this.raisedLayoutY = layoutY - raise;
		super.layoutPosition(super.getLayoutX(), this.raisedLayoutY).layoutSize(super.getWidth(), height);
		this.field.layoutPosition(0D, 0D).layoutSize(super.getWidth(), height);

		final Node selected = this.getSelected();
		double oy = this.direction.isDown() ? size : raise - size;
		for (final Node option : this.optionList) {
			option.layoutSize(super.getDefaultWidth(), size);
			if (option == selected) {
				option.layoutPosition(0D, raise);
				continue;
			}

			option.layoutPosition(0D, oy);
			oy += this.direction.isDown() ? size : -size;
		}
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (!event.getButton().isLeft()) {
			return;
		}

		if (!this.active) {
			if (super.isHovered()) {
				this.activeValue.set(true);
				event.consume();
			}
			return;
		}

		final Node clicked = this.optionList.stream().filter(Node::isHovered).findFirst().orElse(null);
		if (event.isConsumed() || clicked == null && !super.isHovered()) {
			this.activeValue.set(false);
			return;
		}

		if (clicked != null && clicked != this.getSelected()) {
			super.change(super.getValueList().get(this.optionList.indexOf(clicked)));
		}

		this.activeValue.set(false);
		event.consume();
	}

	@Override
	public void detach() {
		this.activeValue.update(false);
	}

	@Override
	protected void layoutValues() {
		this.optionList.forEach(option -> super.remove(option));
		this.optionList.clear();
		if (this.option == null) {
			return;
		}

		for (final V value : super.getValueList()) {
			final Node option = this.option.apply(value);
			this.optionList.add(option);
			super.append(option.visible(() -> this.active || this.isSelected(option)));
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
		this.field = super.part(this.field, field);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T option(final @NonNull Function<V, Node> option) {
		this.option = option;
		this.layoutValues();
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull SelectorDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull Supplier<@NonNull SelectorDirection> direction) {
		return super.follow("direction", direction, value -> this.direction = value);
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final boolean active) {
		return this.activeValue.start(active);
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final @NonNull Signal<Boolean> active) {
		return this.activeValue.bind(active);
	}

	public final <T extends SelectorNode<V>> @NonNull T open() {
		this.activeValue.change(true);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T close() {
		this.activeValue.change(false);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeOpen(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.activeValue.before(true, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onOpen(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.activeValue.on(true, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeClose(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.activeValue.before(false, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onClose(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.activeValue.on(false, listener);
	}

	public static enum SelectorDirection {

		UP,
		DOWN;

		public final boolean isDown() {
			return this == SelectorDirection.DOWN;
		}

	}

}