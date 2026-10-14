package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SwitchNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, String>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, String>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private final IntegerSignal      stateIndex;
	private final ListSignal<String> stateList;

	private Signal<String>            signal;
	private ISignalSubscriber<String> subscription;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = new ListSignal<>();
		this.stateIndex = new IntegerSignal(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.custom((node, signal) -> node.init(node.getUi())));
	}

	public final <T extends SwitchNode> @NonNull T states(final @NonNull String @NonNull... states) {
		return this.states(Signal.from(states).map(Arrays::asList));
	}

	public final <T extends SwitchNode> @NonNull T states(final @NonNull Supplier<@NonNull List<String>> states) {
		return super.follow("states", states, value -> {
			if (value.isEmpty()) {
				throw new IllegalArgumentException("A switch needs at least one state, pass them to states(...)");
			}

			this.configure(new LinkedList<>(value));
		});
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		return this.index(Signal.from(index));
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull Supplier<Integer> index) {
		return super.follow("index", index, value -> this.select(value, false));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String state) {
		return this.state(Signal.from(state));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull Supplier<@NonNull String> state) {
		return super.follow("state", state, value -> {
			if (this.stateList.peek() == null || !this.stateList.peek().contains(value)) {
				throw new IllegalArgumentException("The state " + value + " is not in the state list " + this.stateList.peek());
			}

			this.select(this.stateList.peek().indexOf(value), false);
		});
	}

	public final @NonNull String getState() {
		return this.stateList.get(this.stateIndex.get());
	}

	public final <T extends SwitchNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.stateList.peek() != null && this.stateList.peek().contains(value)) {
				this.select(this.stateList.peek().indexOf(value), false);
			}
		});
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, String>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SwitchNode> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, String>> listener) {
		return super.listen(SwitchNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, String>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, String>> listener) {
		return super.listen(SwitchNode.CHANGE, priority, listener);
	}

	protected final void chooseState(final @NonNull String state) {
		if (this.stateList.peek() == null || !this.stateList.peek().contains(state)) {
			throw new IllegalArgumentException("The state " + state + " is not in the state list " + this.stateList.peek());
		}

		this.select(this.stateList.peek().indexOf(state), true);
	}

	private void select(final int index, final boolean user) {
		if (this.stateList.peek() == null || index < 0 || index >= this.stateList.peek().size()) {
			throw new IllegalArgumentException("The index " + index + " is out of the state list " + this.stateList.peek());
		}

		this.change(this.stateList.peek(), index, this.stateList.peek().get(this.stateIndex.peek()), user);
	}

	private void configure(final List<String> stateList) {
		final String previous = this.stateList.peek() == null || this.stateList.peek().isEmpty() ? null : this.stateList.peek().get(this.stateIndex.peek());
		this.change(stateList, Math.max(0, stateList.indexOf(previous)), previous, false);
	}

	private void change(final List<String> stateList, final int index, final String previous, final boolean user) {
		final String state = stateList.get(index);
		if (state.equals(previous)) {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			return;
		}

		super.change(SwitchNode.BEFORE_CHANGE, SwitchNode.CHANGE, previous, state, user, () -> {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			super.sync(this.signal, state);
		});
	}

}