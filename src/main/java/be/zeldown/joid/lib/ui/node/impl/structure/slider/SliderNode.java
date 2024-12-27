package be.zeldown.joid.lib.ui.node.impl.structure.slider;

import java.util.LinkedHashSet;
import java.util.Set;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.structure.slider.callback.NodeSliderChangeCallback;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderNode<O> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSliderChangeCallback.class);

	private O      value;
	private Set<O> valueSet;

	private SliderCursorNode cursor;
	private Signal<O>        signal;

	protected SliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.valueSet = new LinkedHashSet<>();
	}

	@Override
	public void init(final @NonNull UI ui) {
		if (this.value == null || this.valueSet.isEmpty() || this.cursor == null) {
			return;
		}

		int index = 0;
		for (final O value : this.valueSet) {
			if (value.equals(this.value)) {
				break;
			}

			index++;
		}

		this.cursor.x((super.getWidth() - this.cursor.getWidth()) * ((double)index / (double)(this.valueSet.size() - 1)));
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		if (this.value == null || this.valueSet.isEmpty() || this.cursor == null) {
			return;
		}

		final float percent = Math.min(1, Math.max(0, (float) this.cursor.getX() / (float) (super.getWidth() - this.cursor.getWidth())));
		final O newValue = (O) this.valueSet.toArray()[(int) ((this.valueSet.size() - 1) * percent)];
		if (!this.value.equals(newValue)) {
			super.executeCallback(SliderNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.value = newValue;
				if (this.signal != null) {
					this.signal.set(this.value);
				}
			}, newValue);
		}

		this.drawSlider(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		if (!super.isHovered()) {
			return;
		}

		context.cancel(() -> {
			this.cursor.x(mouseX - super.getAbsoluteX() - this.cursor.getWidth() / 2);
			this.cursor.dragging(true);
		});
	}

	public abstract void drawSlider(final double mouseX, final double mouseY);

	public final <T extends SliderNode<O>> @NonNull T cursor(final @NonNull SliderCursorNode cursor) {
		if (this.cursor != null) {
			this.getChildren().remove(this.cursor);
		}

		this.cursor = cursor.slider(this).attach(this);
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T valueSet(final @NonNull Set<O> valueSet, final @NonNull O value) {
		if (!valueSet.contains(value)) {
			throw new IllegalArgumentException("The value is not in the value set");
		}

		this.valueSet = valueSet;
		this.value    = value;
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T value(final @NonNull O value) {
		if (!this.valueSet.contains(value)) {
			throw new IllegalArgumentException("The value is not in the value set");
		}

		this.value = value;
		if (super.getUi() != null) {
			this.init(super.getUi());
		}
		return (T) this;
	}

	public final <T extends SliderNode<O>> @NonNull T signal(final @NonNull Signal<O> signal) {
		this.signal = signal;
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends SliderNode<O>> @NonNull T onChange(final @NonNull NodeSliderChangeCallback<T, O> callback) {
		super.registerCallback(SliderNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

}