package dev.joid.lib.ui.node.impl.control.choice;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.internal.RangeList;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public abstract class BaseChoiceNode<V> extends Node {

	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");
	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");

	private final transient List<V>             valueView;
	private final transient InteractiveValue<V> choiceValue;

	private V       value;
	private int     pendingIndex;
	private List<V> valueList;

	protected BaseChoiceNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.valueList    = Collections.emptyList();
		this.valueView    = new ValueView();
		this.pendingIndex = -1;
		this.choiceValue  = InteractiveValue.create(super.getBindings(), "value", () -> this.value, (final V value) -> {
			this.value = value;
			this.pendingIndex = -1;
		})
		.clamp(value -> this.valueList.isEmpty() || this.valueList.contains(value) ? value : this.fallback(value))
		.events(BaseChoiceNode.BEFORE_CHANGE, (previous, value) -> NodeChangeEvent.Pre.create(this, previous, value), BaseChoiceNode.CHANGE, (previous, value) -> NodeChangeEvent.Post.create(this, previous, value));

		super.<BaseChoiceNode<V>>require("values", "call values(...) before attaching it", node -> !node.valueList.isEmpty());
	}

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
		return this.valueView;
	}

	@SafeVarargs
	public final <T extends BaseChoiceNode<V>> @NonNull T values(final @NonNull V @NonNull... values) {
		return this.values(Signal.from(Arrays.asList(values)));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T values(final @NonNull Supplier<@NonNull List<V>> values) {
		return super.follow("values", values, this::configure);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T value(final @NonNull V value) {
		return super.start("value", this.requireOption(this.valueList, value), this::choose);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T value(final @NonNull Signal<V> value) {
		return super.bind("value", value, this::choose, (final V current) -> current);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T valueIndex(final int valueIndex) {
		return super.start("value", valueIndex, index -> this.select(this.requireIndex(this.valueList, index)));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T valueIndex(final @NonNull Signal<Integer> valueIndex) {
		return super.bind("value", valueIndex, this::select, (final V value) -> this.valueList.indexOf(value));
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T change(final @NonNull V value) {
		final V option = this.requireOption(this.valueList, value);
		if (Objects.equals(option, this.value)) {
			this.pendingIndex = -1;
		}

		this.choiceValue.change(option);
		return (T) this;
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, V>> listener) {
		return this.choiceValue.before(listener);
	}

	public final <T extends BaseChoiceNode<V>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, V>> listener) {
		return this.choiceValue.on(listener);
	}

	protected void createOptions() {}

	protected V fallback(final V value) {
		return this.valueList.contains(this.value) ? this.value : this.valueList.get(0);
	}

	private void select(final int index) {
		if (index < 0 || index >= this.valueList.size()) {
			this.pendingIndex = index;
			if (!this.valueList.isEmpty()) {
				super.sync("value", this.value);
			}
			return;
		}

		this.choose(this.valueList.get(index));
	}

	private void configure(final List<V> values) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException("The " + JOIDLog.nameOf(this.getClass()) + " needs at least one value, pass them to values(...)");
		}

		if (values.equals(this.valueList) && this.pendingIndex < 0) {
			return;
		}

		final boolean bound = super.getBindings().isBound("value");
		final int index = bound || this.pendingIndex < 0 ? this.pendingIndex : this.requireIndex(values, this.pendingIndex);
		final boolean valid = index >= 0 && index < values.size();
		if (this.valueList.isEmpty() && !valid && this.value != null && !bound) {
			this.requireOption(values, this.value);
		}

		this.valueList = values instanceof RangeList ? values : new ArrayList<>(values);
		this.createOptions();
		if (valid) {
			this.choose(values.get(index));
		} else {
			this.choiceValue.reapply();
			this.pendingIndex = index;
		}
	}

	private void choose(final V value) {
		this.pendingIndex = -1;
		this.choiceValue.update(value);
	}

	private V requireOption(final List<V> values, final V value) {
		if (!values.isEmpty() && !values.contains(value)) {
			throw new IllegalArgumentException("The value " + value + " is not an option of the " + JOIDLog.nameOf(this.getClass()) + " " + values + ", pass one of them or add it with values(...)");
		}

		return value;
	}

	private int requireIndex(final List<V> values, final int index) {
		if (!values.isEmpty() && (index < 0 || index >= values.size())) {
			throw new IllegalArgumentException("The index " + index + " is out of the values " + values + " of the " + JOIDLog.nameOf(this.getClass()) + ", pass an index from 0 to " + (values.size() - 1));
		}

		return index;
	}

	private final class ValueView extends AbstractList<V> implements RandomAccess {

		@Override
		public int size() {
			return BaseChoiceNode.this.valueList.size();
		}

		@Override
		public V get(final int index) {
			return BaseChoiceNode.this.valueList.get(index);
		}

		@Override
		public int indexOf(final Object value) {
			return BaseChoiceNode.this.valueList.indexOf(value);
		}

		@Override
		public int lastIndexOf(final Object value) {
			return BaseChoiceNode.this.valueList.lastIndexOf(value);
		}

		@Override
		public boolean contains(final Object value) {
			return BaseChoiceNode.this.valueList.contains(value);
		}

		private List<V> getList() {
			return BaseChoiceNode.this.valueList;
		}

		@Override
		public boolean equals(final Object object) {
			return BaseChoiceNode.this.valueList.equals(object instanceof BaseChoiceNode<?>.ValueView ? ((BaseChoiceNode<?>.ValueView) object).getList() : object);
		}

		@Override
		public int hashCode() {
			return BaseChoiceNode.this.valueList.hashCode();
		}

		@Override
		public String toString() {
			return BaseChoiceNode.this.valueList.toString();
		}

	}

}