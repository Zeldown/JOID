package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import dev.joid.lib.signal.ISignalSubscriber;
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

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderNode<O> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private O      value;
	private Set<O> valueSet;

	private Signal<O>            signal;
	private SliderThumbNode      thumb;
	private ISignalSubscriber<O> subscription;

	protected SliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.valueSet = new LinkedHashSet<>();
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.place();
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.value == null || this.valueSet.isEmpty() || this.thumb == null) {
			return;
		}

		this.thumb.y((super.getHeight() - this.thumb.getHeight()) / 2D);
		this.pick();

		this.drawSlider(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !super.isHovered()) {
			return;
		}

		this.thumb.x(event.getMouseX() - super.getAbsoluteX() - this.thumb.getWidth() / 2);
		this.thumb.dragging(true);
		event.consume();
	}

	public abstract void drawSlider(final double mouseX, final double mouseY);

	public final float getProgress() {
		if (this.thumb == null || super.getWidth() <= this.thumb.getWidth()) {
			return 0F;
		}

		return Math.min(1F, Math.max(0F, (float) this.thumb.getX() / (float) (super.getWidth() - this.thumb.getWidth())));
	}

	public final <T extends SliderNode<O>> @NonNull T thumb(final @NonNull SliderThumbNode thumb) {
		if (this.thumb != null) {
			this.getChildren().remove(this.thumb);
		}

		this.thumb = thumb.slider(this).attach(this);
		return (T) this;
	}

	@SafeVarargs
	public final <T extends SliderNode<O>> @NonNull T values(final @NonNull O @NonNull... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("A slider needs at least one option");
		}

		this.valueSet = new LinkedHashSet<>(Arrays.asList(values));
		if (!this.valueSet.contains(this.value)) {
			this.change(values[0]);
		} else if (super.getUi() != null) {
			this.place();
		}
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull O value) {
		return this.value(Signal.from(value));
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull Supplier<@NonNull O> value) {
		return super.follow("value", value, option -> {
			if (!this.valueSet.contains(option)) {
				throw new IllegalArgumentException("The value " + option + " is not an option of the slider " + this.valueSet + ", pass one of them or add it with values(...)");
			}

			this.change(option);
		});
	}

	public final <T extends SliderNode<O>> @NonNull T signal(final @NonNull Signal<O> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.valueSet.contains(value)) {
				this.change(value);
			}
		});
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
		if (this.value == null || this.valueSet.isEmpty() || this.thumb == null) {
			return;
		}

		this.pick();
		this.place();
	}

	private void change(final O value) {
		if (value.equals(this.value)) {
			return;
		}

		super.change(SliderNode.BEFORE_CHANGE, SliderNode.CHANGE, this.value, value, false, () -> {
			this.value = value;
			super.sync(this.signal, value);
			if (super.getUi() != null) {
				this.place();
			}
		});
	}

	private void pick() {
		final O newValue = (O) this.valueSet.toArray()[Math.round((this.valueSet.size() - 1) * this.getProgress())];
		if (this.value.equals(newValue)) {
			return;
		}

		final boolean changed = super.change(SliderNode.BEFORE_CHANGE, SliderNode.CHANGE, this.value, newValue, true, () -> {
			this.value = newValue;
			super.sync(this.signal, newValue);
		});

		if (!changed) {
			this.thumb.dragging(false);
			this.place();
		}
	}

	private void place() {
		if (this.value == null || this.valueSet.isEmpty() || this.thumb == null) {
			return;
		}

		if (this.valueSet.size() == 1) {
			this.thumb.x(0);
			return;
		}

		int index = 0;
		for (final O value : this.valueSet) {
			if (value.equals(this.value)) {
				break;
			}

			index++;
		}

		this.thumb.x((super.getWidth() - this.thumb.getWidth()) * ((double) index / (double) (this.valueSet.size() - 1)));
	}

}