package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.NonNull;

public abstract class SwitchNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, String>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, String>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private final IntegerSignal      stateIndex;
	private final ListSignal<String> stateList;

	private String pendingState;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = new ListSignal<>();
		this.stateIndex = new IntegerSignal(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.custom((node, signal) -> node.init(node.getUi())));
		super.<SwitchNode>require("states", "call states(...) before attaching it", node -> !node.stateList.peek().isEmpty());
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
		return super.start("state", index, value -> this.select(value, false));
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull Signal<Integer> index) {
		return super.bind("state", index, value -> {
			if (this.stateList.peek().isEmpty() || value >= 0 && value < this.stateList.peek().size()) {
				this.select(value, false);
			}
		}, (final String state) -> this.stateList.peek().indexOf(state));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String state) {
		return super.start("state", state, value -> this.choose(value, false));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull Signal<String> state) {
		return super.bind("state", state, value -> {
			if (this.stateList.peek().isEmpty() || this.stateList.peek().contains(value)) {
				this.choose(value, false);
			}
		});
	}

	public final @NonNull String getState() {
		final List<String> stateList = this.stateList.get();
		if (stateList.isEmpty()) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no states, call states(...) before getState()");
		}

		return stateList.get(this.stateIndex.get());
	}

	public final int getStateIndex() {
		return this.stateIndex.get();
	}

	public final @NonNull List<@NonNull String> getStateList() {
		return Collections.unmodifiableList(this.stateList.get());
	}

	public final <T extends SwitchNode> @NonNull T change(final @NonNull String state) {
		this.choose(state, true);
		return (T) this;
	}

	public final <T extends SwitchNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, String>> listener) {
		return super.listen(SwitchNode.BEFORE_CHANGE, listener);
	}

	public final <T extends SwitchNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, String>> listener) {
		return super.listen(SwitchNode.CHANGE, listener);
	}

	private void choose(final String state, final boolean refusable) {
		if (this.stateList.peek().isEmpty()) {
			this.pendingState = state;
			return;
		}

		this.select(this.indexOf(this.stateList.peek(), state), refusable);
	}

	private void select(final int index, final boolean refusable) {
		if (this.stateList.peek().isEmpty()) {
			this.pendingState = null;
			this.stateIndex.set(index);
			return;
		}

		this.change(this.stateList.peek(), this.checkIndex(this.stateList.peek(), index), this.stateList.peek().get(this.stateIndex.peek()), refusable);
	}

	private void configure(final List<String> stateList) {
		final String previous = this.stateList.peek().isEmpty() ? null : this.stateList.peek().get(this.stateIndex.peek());
		final String pending = this.pendingState;
		this.pendingState = null;
		if (pending != null) {
			this.change(stateList, this.indexOf(stateList, pending), previous, false);
		} else if (previous == null) {
			this.change(stateList, this.checkIndex(stateList, this.stateIndex.peek()), null, false);
		} else {
			this.change(stateList, Math.max(0, stateList.indexOf(previous)), previous, false);
		}
	}

	private int checkIndex(final List<String> stateList, final int index) {
		if (index < 0 || index >= stateList.size()) {
			throw new IllegalArgumentException("The index " + index + " is out of the state list " + stateList + ", pass an index from 0 to " + (stateList.size() - 1));
		}

		return index;
	}

	private int indexOf(final List<String> stateList, final String state) {
		if (!stateList.contains(state)) {
			throw new IllegalArgumentException("The state " + state + " is not in the state list " + stateList + ", pass one of them or add it with states(...)");
		}

		return stateList.indexOf(state);
	}

	private void change(final List<String> stateList, final int index, final String previous, final boolean refusable) {
		final String state = stateList.get(index);
		if (state.equals(previous)) {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			return;
		}

		super.change(SwitchNode.BEFORE_CHANGE, SwitchNode.CHANGE, previous, state, refusable, () -> {
			this.stateIndex.set(index);
			this.stateList.set(stateList);
			super.sync("state", state);
		});
	}

}