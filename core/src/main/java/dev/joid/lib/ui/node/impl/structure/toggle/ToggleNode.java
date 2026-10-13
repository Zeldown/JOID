package dev.joid.lib.ui.node.impl.structure.toggle;

import java.util.function.Supplier;

import dev.joid.lib.signal.ISignalSubscriber;
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
public abstract class ToggleNode<F, S> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private boolean                    toggle;
	private Signal<Boolean>            signal;
	private ToggleState<F, S>          state;
	private ISignalSubscriber<Boolean> subscription;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !super.isHovered()) {
			return;
		}

		this.change(!this.toggle, true);
		event.consume();
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		return this.toggle(Signal.from(toggle));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final @NonNull Supplier<Boolean> toggle) {
		return super.follow("toggle", toggle, value -> this.change(value, false));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = new ToggleState<>(toggle, back);
		return (T) this;
	}

	public final <T> @NonNull T getValue() {
		return (T) (this.toggle ? this.state.getToggle() : this.state.getBack());
	}

	public final <T extends ToggleNode<F, S>> @NonNull T signal(final @NonNull Signal<Boolean> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> this.change(value, false));
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(ToggleNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(ToggleNode.CHANGE, priority, listener);
	}

	private void change(final boolean toggle, final boolean user) {
		if (this.toggle == toggle) {
			return;
		}

		super.change(ToggleNode.BEFORE_CHANGE, ToggleNode.CHANGE, this.toggle, toggle, user, () -> {
			this.toggle = toggle;
			super.sync(this.signal, toggle);
		});
	}

}