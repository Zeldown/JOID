package dev.joid.lib.ui.node.impl.control.choice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public abstract class BaseChoiceNode<V> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private final transient InteractiveValue<V> choiceValue;

	private final List<V> valueList;

	private V   value;
	private int pendingIndex;

	protected BaseChoiceNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.valueList    = new ArrayList<>();
		this.pendingIndex = -1;
		this.choiceValue  = InteractiveValue.create(super.getBindings(), "value", () -> this.value, (final V value) -> {
			this.value = value;
			this.pendingIndex = -1;
		})
		.clamp(value -> this.valueList.isEmpty() || this.valueList.contains(value) ? value : this.value)
		.events(BaseChoiceNode.BEFORE_CHANGE, (previous, value) -> NodeChangeEvent.Pre.create(this, previous, value), BaseChoiceNode.CHANGE, (previous, value) -> NodeChangeEvent.Post.create(this, previous, value));

		super.<BaseChoiceNode<V>>require("values", "call values(...) before attaching it", node -> !node.valueList.isEmpty());
	}

	protected void createOptions() {}

	public final V getValue() {
		if (this.valueList.isEmpty()) {
			throw new IllegalStateException("The " + JOIDLog.nameOf(this.getClass()) + " has no values, call values(...) before getValue()");
		}

		return this.value;
	}

	public final int getValueIndex() {
		return this.valueList.indexOf(this.value);
	}

	public final @NonNull List<V> getValueList() {
		return Collections.unmodifiableList(this.valueList);
	}

	@SafeVarargs
	public final <T extends BaseChoiceNode<V>> @NonNull T values(final @NonNull V @NonNull... values) {
		return this.values(Signal.from(Arrays.asList(values)));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T values(final @NonNull Supplier<@NonNull List<V>> values) {
		return super.follow("values", values, this::configure);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T value(final @NonNull V value) {
		return this.choiceValue.start(this.requireOption(this.valueList, value));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T value(final @NonNull Signal<V> value) {
		return this.choiceValue.bind(value);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T valueIndex(final int valueIndex) {
		return super.start("value", valueIndex, index -> this.select(this.requireIndex(this.valueList, index)));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T valueIndex(final @NonNull Signal<Integer> valueIndex) {
		return super.bind("value", valueIndex, index -> {
			if (this.valueList.isEmpty() || index >= 0 && index < this.valueList.size()) {
				this.select(index);
			}
		}, (final V value) -> this.valueList.indexOf(value));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T change(final @NonNull V value) {
		this.choiceValue.change(this.requireOption(this.valueList, value));
		return (T) this;
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, V>> listener) {
		return this.choiceValue.before(listener);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, V>> listener) {
		return this.choiceValue.on(listener);
	}

	private void select(final int index) {
		if (this.valueList.isEmpty()) {
			this.pendingIndex = index;
			return;
		}

		this.choiceValue.update(this.valueList.get(index));
	}

	private void configure(final @NonNull List<V> values) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException("The " + JOIDLog.nameOf(this.getClass()) + " needs at least one value, pass them to values(...)");
		}

		if (values.equals(this.valueList) && this.pendingIndex < 0) {
			return;
		}

		final boolean bound = super.getBindings().isBound("value");
		final int index = bound || this.pendingIndex < 0 ? this.pendingIndex : this.requireIndex(values, this.pendingIndex);
		final V current = index >= 0 && index < values.size() ? values.get(index) : this.value;
		if (this.valueList.isEmpty() && current != null && !bound) {
			this.requireOption(values, current);
		}

		this.pendingIndex = -1;
		this.valueList.clear();
		this.valueList.addAll(values);
		this.createOptions();
		this.choiceValue.update(values.contains(current) ? current : values.get(0));
	}

	private V requireOption(final @NonNull List<V> values, final V value) {
		if (!values.isEmpty() && !values.contains(value)) {
			throw new IllegalArgumentException("The value " + value + " is not an option of the " + JOIDLog.nameOf(this.getClass()) + " " + values + ", pass one of them or add it with values(...)");
		}

		return value;
	}

	private int requireIndex(final @NonNull List<V> values, final int index) {
		if (!values.isEmpty() && (index < 0 || index >= values.size())) {
			throw new IllegalArgumentException("The index " + index + " is out of the values " + values + " of the " + JOIDLog.nameOf(this.getClass()) + ", pass an index from 0 to " + (values.size() - 1));
		}

		return index;
	}

}