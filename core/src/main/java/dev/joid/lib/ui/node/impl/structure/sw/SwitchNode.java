package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.sw.callback.NodeSwitchChangeCallback;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SwitchNode extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSwitchChangeCallback.class);

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
				throw new IllegalArgumentException("The state list is empty");
			}

			this.configure(new LinkedList<>(value), 0);
		});
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		return this.index(Signal.from(index));
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull Supplier<Integer> index) {
		return super.follow("index", index, this::select);
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String state) {
		return this.state(Signal.from(state));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull Supplier<@NonNull String> state) {
		return super.follow("state", state, value -> {
			if (this.stateList.peek() == null || !this.stateList.peek().contains(value)) {
				throw new IllegalArgumentException("The state " + value + " is not in the state list " + this.stateList.peek());
			}

			this.select(this.stateList.peek().indexOf(value));
		});
	}

	public final @NonNull String getState() {
		return this.stateList.get(this.stateIndex.get());
	}

	public final <T extends SwitchNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.stateList.peek() != null && this.stateList.peek().contains(value)) {
				this.select(this.stateList.peek().indexOf(value));
			}
		});
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull NodeSwitchChangeCallback<T> callback) {
		super.registerCallback(SwitchNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	private void select(final int index) {
		if (this.stateList.peek() == null || index < 0 || index >= this.stateList.peek().size()) {
			throw new IllegalArgumentException("The index " + index + " is out of the state list " + this.stateList.peek());
		}

		this.change(this.stateList.peek(), index, this.stateList.peek().get(this.stateIndex.peek()));
	}

	private void configure(final List<String> stateList, final int index) {
		final String previous = this.stateList.peek() == null || this.stateList.peek().isEmpty() ? null : this.stateList.peek().get(this.stateIndex.peek());
		this.change(stateList, index, previous);
	}

	private void change(final List<String> stateList, final int index, final String previous) {
		final String state = stateList.get(index);
		if (state.equals(previous)) {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			return;
		}

		super.executeCallback(SwitchNode.CALLBACK_CHANGE, DispatchContext.create(), () -> {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			super.sync(this.signal, state);
		}, state);
	}

}