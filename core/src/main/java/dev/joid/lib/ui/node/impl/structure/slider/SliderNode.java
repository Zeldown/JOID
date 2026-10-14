package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

public abstract class SliderNode<O> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");

	@Getter private O value;
	private Set<O>    valueSet;

	@Getter private SliderThumbNode thumb;

	private O pendingValue;

	protected SliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.valueSet = new LinkedHashSet<>();

		super.<SliderNode<O>>require("thumb", "call thumb(...) before attaching it", node -> node.thumb != null);
		super.<SliderNode<O>>require("values", "call values(...) or range(...) before attaching it", node -> !node.valueSet.isEmpty());
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.place();
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		super.layoutPosition(this.thumb, this.thumb.getLayoutX(), (super.getHeight() - this.thumb.getHeight()) / 2D);
		this.pick();

		this.drawSlider(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		super.layoutPosition(this.thumb, event.getMouseX() - super.getAbsoluteX() - this.thumb.getWidth() / 2, this.thumb.getLayoutY());
		this.thumb.dragging(true);
		event.consume();
	}

	public abstract void drawSlider(final double mouseX, final double mouseY);

	public final @NonNull Set<O> getValueSet() {
		return Collections.unmodifiableSet(this.valueSet);
	}

	public final float getProgress() {
		if (this.thumb == null || super.getWidth() <= this.thumb.getWidth()) {
			return 0F;
		}

		return Math.min(1F, Math.max(0F, (float) this.thumb.getX() / (float) (super.getWidth() - this.thumb.getWidth())));
	}

	public final <T extends SliderNode<O>> @NonNull T thumb(final @NonNull SliderThumbNode thumb) {
		if (this.thumb != null) {
			super.remove(this.thumb);
		}

		this.thumb = thumb.slider(this).attach(this);
		return (T) this;
	}

	@SafeVarargs
	public final <T extends SliderNode<O>> @NonNull T values(final @NonNull O @NonNull... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("A slider needs at least one option, got none: pass them to values(...)");
		}

		this.valueSet = new LinkedHashSet<>(Arrays.asList(values));
		final O pending = this.pendingValue;
		this.pendingValue = null;
		if (pending != null) {
			this.choose(pending, false);
		} else if (!this.valueSet.contains(this.value)) {
			this.change(values[0], false);
		} else if (super.hasUi()) {
			this.place();
		}
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull O value) {
		return super.userProperty("value", value, (final SliderNode<O> node, final O option) -> node.choose(option, false));
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull Signal<O> value) {
		return super.userProperty("value", value, (final SliderNode<O> node, final O option) -> {
			if (node.valueSet.isEmpty() || node.valueSet.contains(option)) {
				node.choose(option, false);
			}
		});
	}

	public final <T extends SliderNode<O>> @NonNull T change(final @NonNull O value) {
		this.choose(value, true);
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, O>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SliderNode<O>> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, O>> listener) {
		return super.listen(SliderNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends SliderNode<O>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, O>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SliderNode<O>> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, O>> listener) {
		return super.listen(SliderNode.CHANGE, priority, listener);
	}

	protected final void release() {
		this.pick();
		this.place();
	}

	private void choose(final O value, final boolean refusable) {
		if (this.valueSet.isEmpty()) {
			this.pendingValue = value;
			return;
		}

		if (!this.valueSet.contains(value)) {
			throw new IllegalArgumentException("The value " + value + " is not an option of the slider " + this.valueSet + ", pass one of them or add it with values(...)");
		}

		this.change(value, refusable);
	}

	private void change(final O value, final boolean refusable) {
		if (value.equals(this.value)) {
			return;
		}

		super.change(SliderNode.BEFORE_CHANGE, SliderNode.CHANGE, this.value, value, refusable, () -> {
			this.value = value;
			super.sync("value", value);
		});

		if (super.hasUi()) {
			this.place();
		}
	}

	private void pick() {
		final O newValue = (O) this.valueSet.toArray()[Math.round((this.valueSet.size() - 1) * this.getProgress())];
		if (this.value.equals(newValue)) {
			return;
		}

		final boolean changed = super.change(SliderNode.BEFORE_CHANGE, SliderNode.CHANGE, this.value, newValue, true, () -> {
			this.value = newValue;
			super.sync("value", newValue);
		});

		if (!changed) {
			this.thumb.dragging(false);
			this.place();
		}
	}

	private void place() {
		if (this.valueSet.size() == 1) {
			super.layoutPosition(this.thumb, 0D, this.thumb.getLayoutY());
			return;
		}

		int index = 0;
		for (final O value : this.valueSet) {
			if (value.equals(this.value)) {
				break;
			}

			index++;
		}

		super.layoutPosition(this.thumb, (super.getWidth() - this.thumb.getWidth()) * ((double) index / (double) (this.valueSet.size() - 1)), this.thumb.getLayoutY());
	}

}