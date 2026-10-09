package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.slider.callback.NodeSliderChangeCallback;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderNode<O> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSliderChangeCallback.class);

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
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (context.isCancelled() || !super.isHovered()) {
			return;
		}

		context.cancel(() -> {
			this.thumb.x(mouseX - super.getAbsoluteX() - this.thumb.getWidth() / 2);
			this.thumb.dragging(true);
		});
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

	public final <T extends SliderNode<O>> @NonNull T valueSet(final @NonNull Set<O> valueSet, final @NonNull O value) {
		if (!valueSet.contains(value)) {
			throw new IllegalArgumentException("The value is not in the value set");
		}

		this.valueSet = valueSet;
		this.change(value);
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull O value) {
		return this.value(Signal.from(value));
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull Supplier<@NonNull O> value) {
		return super.follow("value", value, option -> {
			if (!this.valueSet.contains(option)) {
				throw new IllegalArgumentException("The value is not in the value set");
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

	public final <T extends SliderNode<O>> @NonNull T onChange(final @NonNull NodeSliderChangeCallback<T, O> callback) {
		super.registerCallback(SliderNode.CALLBACK_CHANGE, callback);
		return (T) this;
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

		super.executeCallback(SliderNode.CALLBACK_CHANGE, DispatchContext.create(), () -> {
			this.value = value;
			super.sync(this.signal, value);
			if (super.getUi() != null) {
				this.place();
			}
		}, value);
	}

	private void pick() {
		final O newValue = (O) this.valueSet.toArray()[Math.round((this.valueSet.size() - 1) * this.getProgress())];
		if (this.value.equals(newValue)) {
			return;
		}

		super.executeCallback(SliderNode.CALLBACK_CHANGE, DispatchContext.create(), () -> {
			this.value = newValue;
			super.sync(this.signal, newValue);
		}, newValue);

		if (!this.value.equals(newValue)) {
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