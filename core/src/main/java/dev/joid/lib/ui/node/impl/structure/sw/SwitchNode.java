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

	private Signal<String> signal;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = new ListSignal<>();
		this.stateIndex = new IntegerSignal(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);
		super.watch(this.stateIndex, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String... stateList) {
		assert stateList.length > 0;
		this.configure(new LinkedList<>(Arrays.asList(stateList)), 0);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final int index) {
		assert !stateList.isEmpty() && index >= 0 && index < stateList.size();
		this.configure(stateList, index);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final @NonNull String state) {
		assert !stateList.isEmpty() && stateList.contains(state);
		this.configure(new LinkedList<>(stateList), stateList.indexOf(state));
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		assert this.stateList.getOrDefault() != null && !this.stateList.isEmpty() && index >= 0 && index < this.stateList.size();
		this.change(index, this.getState());
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull String state) {
		assert this.stateList.getOrDefault() != null && !this.stateList.isEmpty() && this.stateList.contains(state);
		this.change(this.stateList.indexOf(state), this.getState());
		return (T) this;
	}

	public final @NonNull String getState() {
		return this.stateList.get(this.stateIndex.getOrDefault());
	}

	public final <T extends SwitchNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = signal;
		super.bind(signal, value -> {
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
		this.stateList.set(stateList);
		this.change(index, previous);
	}

	private void change(final int index, final String previous) {
		final String state = this.stateList.get(index);
		if (state.equals(previous)) {
			this.stateIndex.set(index);
			return;
		}

		super.executeCallback(SwitchNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.stateIndex.set(index);
			super.sync(this.signal, state);
		}, state);
	}

}