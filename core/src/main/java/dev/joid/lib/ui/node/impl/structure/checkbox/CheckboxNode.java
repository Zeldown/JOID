package dev.joid.lib.ui.node.impl.structure.checkbox;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class CheckboxNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private boolean         checked;
	private Signal<Boolean> signal;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.change(!this.checked, true);
		event.consume();
	}

	public final <T extends CheckboxNode> @NonNull T checked(final boolean checked) {
		return this.checked(Signal.from(checked));
	}

	public final <T extends CheckboxNode> @NonNull T checked(final @NonNull Supplier<Boolean> checked) {
		return super.follow("checked", checked, (final CheckboxNode node, final Boolean value) -> node.change(value, false));
	}

	public final <T extends CheckboxNode> @NonNull T signal(final @NonNull Signal<Boolean> signal) {
		this.signal = super.writable(signal);
		return super.bind("signal", signal, (final CheckboxNode node, final Boolean value) -> node.change(value, false));
	}

	public final <T extends CheckboxNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends CheckboxNode> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(CheckboxNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(CheckboxNode.CHANGE, priority, listener);
	}

	private void change(final boolean checked, final boolean user) {
		if (this.checked == checked) {
			return;
		}

		super.change(CheckboxNode.BEFORE_CHANGE, CheckboxNode.CHANGE, this.checked, checked, user, () -> {
			this.checked = checked;
			super.sync(this.signal, checked);
		});
	}

}