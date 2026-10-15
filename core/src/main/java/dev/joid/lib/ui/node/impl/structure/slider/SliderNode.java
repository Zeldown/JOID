package dev.joid.lib.ui.node.impl.structure.slider;

import java.util.List;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.choice.ChoiceNode;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class SliderNode<V> extends ChoiceNode<V> {

	private Node track;
	private Node thumb;

	private boolean dragging;

	protected SliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.<SliderNode<V>>require("thumb", "give it one with thumb(...)", node -> node.thumb != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		this.thumb.fit(super.getHeight(), super.getHeight());
		if (!this.dragging) {
			this.layoutValues();
			return;
		}

		this.follow(mouseX);
		if (!this.pick()) {
			this.dragging = false;
			this.layoutValues();
		}
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.dragging = true;
		this.follow(event.getMouseX());
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (!this.dragging || !event.getButton().isLeft()) {
			return;
		}

		this.pick();
		this.dragging = false;
		this.layoutValues();
	}

	@Override
	public void detach() {
		this.dragging = false;
	}

	@Override
	protected void layoutValues() {
		if (this.thumb == null) {
			return;
		}

		final int count = super.getValueList().size();
		final double room = super.getWidth() - this.thumb.getWidth();
		this.thumb.layoutPosition(count <= 1 ? 0D : room * Math.max(0, super.getValueIndex()) / (count - 1), (super.getHeight() - this.thumb.getHeight()) / 2D);
	}

	public final float getProgress() {
		final int count = super.getValueList().size();
		if (this.thumb == null || count <= 1 || super.getWidth() <= this.thumb.getWidth()) {
			return 0F;
		}

		if (!this.dragging) {
			return Math.max(0, super.getValueIndex()) / (float) (count - 1);
		}

		return Math.min(1F, Math.max(0F, (float) this.thumb.getX() / (float) (super.getWidth() - this.thumb.getWidth())));
	}

	public final <T extends SliderNode<V>> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track);
		return (T) this;
	}

	public final <T extends SliderNode<V>> @NonNull T thumb(final @NonNull Node thumb) {
		this.thumb = super.part(this.thumb, thumb);
		return (T) this;
	}

	private void follow(final double mouseX) {
		this.thumb.layoutPosition(Math.min(Math.max(0D, mouseX - super.getAbsoluteX() - this.thumb.getWidth() / 2D), super.getWidth() - this.thumb.getWidth()), (super.getHeight() - this.thumb.getHeight()) / 2D);
	}

	private boolean pick() {
		final List<V> values = super.getValueList();
		final V value = values.get(Math.round((values.size() - 1) * this.getProgress()));
		super.change(value);
		return value.equals(super.getValue());
	}

}