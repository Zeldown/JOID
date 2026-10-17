package dev.joid.lib.ui.node.impl.control.selector;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.impl.control.choice.BaseChoiceNode;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseSelectorNode<V> extends BaseChoiceNode<V> {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> OPEN         = EventType.create("onOpen");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CLOSE        = EventType.create("onClose");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_OPEN  = EventType.create("beforeOpen");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CLOSE = EventType.create("beforeClose");

	private final transient InteractiveValue<Boolean> activeValue;

	@Getter
	private boolean active;

	protected BaseSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.activeValue = InteractiveValue.create(super.getBindings(), "active", () -> this.active, (final Boolean active) -> this.active = active)
		.events(true, BaseSelectorNode.BEFORE_OPEN, (previous, active) -> NodeChangeEvent.Pre.create(this, previous, active), BaseSelectorNode.OPEN, (previous, active) -> NodeChangeEvent.Post.create(this, previous, active))
		.events(false, BaseSelectorNode.BEFORE_CLOSE, (previous, active) -> NodeChangeEvent.Pre.create(this, previous, active), BaseSelectorNode.CLOSE, (previous, active) -> NodeChangeEvent.Post.create(this, previous, active));
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (!event.getButton().isLeft()) {
			return;
		}

		final boolean pressed = !event.isConsumed() && super.isHovered();
		if (this.active || pressed) {
			this.activeValue.set(!this.active);
		}

		if (pressed) {
			event.consume();
		}
	}

	@Override
	public void detach() {
		this.activeValue.update(false);
	}

	public final int getOptionRank(final @NonNull V value) {
		final int index = super.getValueList().indexOf(value);
		final int selected = super.getValueIndex();
		return index == selected ? 0 : index < selected ? index + 1 : index;
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T active(final boolean active) {
		return this.activeValue.start(active);
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T active(final @NonNull Signal<Boolean> active) {
		return this.activeValue.bind(active);
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T open() {
		this.activeValue.change(true);
		return (T) this;
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T close() {
		this.activeValue.change(false);
		return (T) this;
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T beforeOpen(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.activeValue.before(true, listener);
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T onOpen(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.activeValue.on(true, listener);
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T beforeClose(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.activeValue.before(false, listener);
	}

	public final <T extends BaseSelectorNode<V>> @NonNull T onClose(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.activeValue.on(false, listener);
	}

}