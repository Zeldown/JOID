package be.zeldown.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.structure.sw.callback.NodeSwitchChangeCallback;
import be.zeldown.joid.lib.ui.node.property.watch.WatchProperty;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.signal.impl.iterable.ListSignal;
import be.zeldown.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SwitchNode extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSwitchChangeCallback.class);

	private final ListSignal<String> stateList;
	private final IntegerSignal stateIndex;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = new ListSignal<>();
		this.stateIndex = new IntegerSignal(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);
		super.watch(this.stateIndex, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final @NonNull String state) {
		assert !stateList.isEmpty() && stateList.contains(state);
		this.stateList.set(new LinkedList<>(stateList));
		this.stateIndex.set(stateList.indexOf(state));
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull List<String> stateList, final int index) {
		assert !stateList.isEmpty() && index >= 0 && index < stateList.size();
		this.stateList.set(stateList);
		this.stateIndex.set(index);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String... stateList) {
		assert stateList.length > 0;
		this.stateList.set(new LinkedList<>(Arrays.asList(stateList)));
		this.stateIndex.set(0);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull String state) {
		assert this.stateList.getOrDefault() != null && !this.stateList.isEmpty() && this.stateList.contains(state);
		super.executeCallback(SwitchNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.stateIndex.set(this.stateList.indexOf(state));
		}, state);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		assert this.stateList.getOrDefault() != null && !this.stateList.isEmpty() && index >= 0 && index < this.stateList.size();
		super.executeCallback(SwitchNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.stateIndex.set(index);
		}, this.stateList.get(index));
		return (T) this;
	}

	public final @NonNull String getState() {
		return this.stateList.get(this.stateIndex.getOrDefault());
	}

	/* [ Callback Section ] */
	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull NodeSwitchChangeCallback<T> callback) {
		super.registerCallback(SwitchNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

}