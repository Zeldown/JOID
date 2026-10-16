package dev.joid.lib.ui.node.impl.control.slider;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class SliderNode<V> extends BaseSliderNode<V> {

	private Node track;
	private Node thumb;

	private float speed;

	@Getter(AccessLevel.NONE)
	private double thumbX;

	@Getter(AccessLevel.NONE)
	private boolean placed;

	protected SliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.speed = 1F;

		super.inset(() -> this.thumb == null ? 0D : this.thumb.getWidth() / 2D);
		super.<SliderNode<V>>require("thumb", "give it one with thumb(...)", node -> node.thumb != null);
	}

	@Override
	public void placeParts(final double mouseX, final double mouseY) {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		this.thumb.fit(super.getHeight(), super.getHeight());
		final double target = super.getInset() + super.getProgress() * (super.getWidth() - super.getInset() * 2D) - this.thumb.getWidth() / 2D;
		this.thumbX = this.placed && !super.isSliding() && this.speed < 1F ? super.getUi().lerpByFramerate(this.thumbX, target, this.speed, 0.001D) : target;
		this.placed = true;
		this.thumb.layoutPosition(this.thumbX, (super.getHeight() - this.thumb.getHeight()) / 2D);
	}

	@Override
	public void detach() {
		super.detach();
		this.placed = false;
	}

	public final <T extends SliderNode<V>> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track.zindex(-1));
		return (T) this;
	}

	public final <T extends SliderNode<V>> @NonNull T thumb(final @NonNull Node thumb) {
		this.thumb = super.part(this.thumb, thumb);
		return (T) this;
	}

	public final <T extends SliderNode<V>> @NonNull T speed(final float speed) {
		return this.speed(Signal.from(speed));
	}

	public final <T extends SliderNode<V>> @NonNull T speed(final @NonNull Supplier<Float> speed) {
		return super.follow("speed", speed, value -> this.speed = value);
	}

}