package dev.joid.lib.ui.node.impl.structure.sw;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.ComputedSignal;
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

@SuppressWarnings("unchecked")
public abstract class SwitchNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, String>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, String>>  BEFORE_CHANGE = EventType.create("beforeChange");

	@Getter private final IntegerSignal      stateIndex;
	@Getter private final ListSignal<String> stateList;

	@Getter private Signal<String> signal;
	private String                 pendingState;

	protected SwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.stateList = ListSignal.of();
		this.stateIndex = IntegerSignal.of(0);

		super.watch(this.stateList, WatchProperty.CLEAR_CHILDREN, WatchProperty.custom((node, signal) -> node.init(node.getUi())));
		super.<SwitchNode>require("states", "call states(...) before attaching it", node -> !node.stateList.peek().isEmpty());
	}

	public final <T extends SwitchNode> @NonNull T states(final @NonNull String @NonNull... states) {
		return this.states(Signal.from(states).map(Arrays::asList));
	}

	public final <T extends SwitchNode> @NonNull T states(final @NonNull Supplier<@NonNull List<String>> states) {
		return super.follow("states", states, (final SwitchNode node, final List<String> value) -> {
			if (value.isEmpty()) {
				throw new IllegalArgumentException("A switch needs at least one state, pass them to states(...)");
			}

			node.configure(new LinkedList<>(value));
		});
	}

	public final <T extends SwitchNode> @NonNull T index(final int index) {
		final ComputedSignal<Integer> source = Signal.from(index);
		final boolean refusable = source.isConstant();
		return super.follow("index", source, (final SwitchNode node, final Integer value) -> node.select(value, refusable));
	}

	public final <T extends SwitchNode> @NonNull T index(final @NonNull Supplier<Integer> index) {
		return super.follow("index", index, (final SwitchNode node, final Integer value) -> node.select(value, false));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull String state) {
		final ComputedSignal<String> source = Signal.from(state);
		final boolean refusable = source.isConstant();
		return super.follow("state", source, (final SwitchNode node, final String value) -> node.choose(value, refusable));
	}

	public final <T extends SwitchNode> @NonNull T state(final @NonNull Supplier<@NonNull String> state) {
		return super.follow("state", state, (final SwitchNode node, final String value) -> node.choose(value, false));
	}

	public final <T extends SwitchNode> @NonNull T forceState(final @NonNull String state) {
		return super.follow("state", Signal.from(state), (final SwitchNode node, final String value) -> node.choose(value, false));
	}

	public final @NonNull String getState() {
		final List<String> stateList = this.stateList.get();
		if (stateList.isEmpty()) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no states, call states(...) before getState()");
		}

		return stateList.get(this.stateIndex.get());
	}

	public final <T extends SwitchNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = super.writable(signal);
		return super.bind("signal", signal, (final SwitchNode node, final String value) -> {
			if (node.stateList.peek().contains(value)) {
				node.select(node.stateList.peek().indexOf(value), false);
			}
		});
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
		} else if (previous == null && this.signal != null && stateList.contains(this.signal.peek())) {
			this.change(stateList, stateList.indexOf(this.signal.peek()), null, false);
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
			super.sync(this.signal, state);
		});
	}

}