package dev.joid.lib.ui.node.impl.control.checkbox;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseCheckboxNode extends Node {

	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");

	private final transient InteractiveValue<Boolean> checkedValue;

	@Getter
	private boolean checked;

	protected BaseCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.checkedValue = InteractiveValue.create(super.getBindings(), "checked", () -> this.checked, (final Boolean checked) -> this.checked = checked)
		.events(BaseCheckboxNode.BEFORE_CHANGE, (previous, checked) -> NodeChangeEvent.Pre.create(this, previous, checked), BaseCheckboxNode.CHANGE, (previous, checked) -> NodeChangeEvent.Post.create(this, previous, checked));
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.checkedValue.set(!this.checkedValue.get());
		event.consume();
	}

	public final <T extends BaseCheckboxNode> @NonNull T checked(final boolean checked) {
		return this.checkedValue.start(checked);
	}

	public final <T extends BaseCheckboxNode> @NonNull T checked(final @NonNull Signal<Boolean> checked) {
		return this.checkedValue.bind(checked);
	}

	public final <T extends BaseCheckboxNode> @NonNull T change(final boolean checked) {
		this.checkedValue.change(checked);
		return (T) this;
	}

	public final <T extends BaseCheckboxNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.checkedValue.before(listener);
	}

	public final <T extends BaseCheckboxNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.checkedValue.on(listener);
	}

}