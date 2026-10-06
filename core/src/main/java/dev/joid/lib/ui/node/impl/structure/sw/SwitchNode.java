package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.sw.callback.NodeSwitchChangeCallback;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import dev.joid.lib.utils.signal.impl.iterable.ListSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SwitchNode extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSwitchChangeCallback.class);

	private final IntegerSignal      stateIndex;
	private final ListSignal<String> stateList;

	private Signal<String>           signal;
	private SignalSubscriber<String> subscription;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = new ListSignal<>();
		this.stateIndex = new IntegerSignal(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String... stateList) {
		if (stateList.length == 0) {
			throw new IllegalArgumentException("The state list is empty");
		}

		this.configure(new LinkedList<>(Arrays.asList(stateList)), 0);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final int index) {
		if (index < 0 || index >= stateList.size()) {
			throw new IllegalArgumentException("The index " + index + " is out of the state list " + stateList);
		}

		this.configure(new LinkedList<>(stateList), index);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final @NonNull String state) {
		if (!stateList.contains(state)) {
			throw new IllegalArgumentException("The state " + state + " is not in the state list " + stateList);
		}

		this.configure(new LinkedList<>(stateList), stateList.indexOf(state));
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		if (this.stateList.getOrDefault() == null || index < 0 || index >= this.stateList.size()) {
			throw new IllegalArgumentException("The index " + index + " is out of the state list " + this.stateList.getOrDefault());
		}

		this.change(this.stateList.getOrDefault(), index, this.getState());
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull String state) {
		if (this.stateList.getOrDefault() == null || !this.stateList.contains(state)) {
			throw new IllegalArgumentException("The state " + state + " is not in the state list " + this.stateList.getOrDefault());
		}

		this.change(this.stateList.getOrDefault(), this.stateList.indexOf(state), this.getState());
		return (T) this;
	}

	public final @NonNull String getState() {
		return this.stateList.get(this.stateIndex.getOrDefault());
	}

	public final <T extends SwitchNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = signal;
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.stateList.contains(value)) {
				this.index(value);
			}
		});
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull NodeSwitchChangeCallback<T> callback) {
		super.registerCallback(SwitchNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	private void configure(final List<String> stateList, final int index) {
		final String previous = this.stateList.getOrDefault() == null || this.stateList.isEmpty() ? null : this.getState();
		this.change(stateList, index, previous);
	}

	private void change(final List<String> stateList, final int index, final String previous) {
		final String state = stateList.get(index);
		if (state.equals(previous)) {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			return;
		}

		super.executeCallback(SwitchNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			super.sync(this.signal, state);
		}, state);
	}

}