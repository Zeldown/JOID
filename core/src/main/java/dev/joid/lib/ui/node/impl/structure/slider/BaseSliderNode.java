package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.impl.structure.choice.BaseChoiceNode;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseSliderNode<V> extends BaseChoiceNode<V> {

	@Getter private double  inset;
	@Getter private boolean sliding;

	private int    slideIndex;
	private double slideProgress;

	protected BaseSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.sliding) {
			this.slide(mouseX);
		}
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.sliding    = true;
		this.slideIndex = super.getValueIndex();
		this.slide(event.getMouseX());
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (!this.sliding || !event.getButton().isLeft()) {
			return;
		}

		this.slide(event.getMouseX());
		this.sliding = false;
	}

	@Override
	public void detach() {
		this.sliding = false;
	}

	public final double getProgress() {
		if (this.sliding) {
			return this.slideProgress;
		}

		final int count = super.getValueList().size();
		return count <= 1 ? 0D : Math.max(0, super.getValueIndex()) / (double) (count - 1);
	}

	public final <T extends BaseSliderNode<V>> @NonNull T inset(final double inset) {
		return this.inset(Signal.from(inset));
	}

	public final <T extends BaseSliderNode<V>> @NonNull T inset(final @NonNull Supplier<Double> inset) {
		return super.follow("inset", inset, value -> this.inset = value);
	}

	private void slide(final double mouseX) {
		final double room = super.getWidth() - this.inset * 2D;
		this.slideProgress = room <= 0D ? 0D : Math.min(1D, Math.max(0D, (mouseX - super.getAbsoluteX() - this.inset) / room));

		final List<V> values = super.getValueList();
		final int index = (int) Math.round((values.size() - 1) * this.slideProgress);
		if (index != this.slideIndex) {
			this.slideIndex = index;
			super.change(values.get(index));
		}
	}

}