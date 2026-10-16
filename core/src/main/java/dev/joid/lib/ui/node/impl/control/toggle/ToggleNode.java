package dev.joid.lib.ui.node.impl.control.toggle;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class ToggleNode<F, S> extends BaseToggleNode<F, S> {

	private Node knob;
	private Node track;

	private float speed;
	private float knobProgress;

	@Getter(AccessLevel.NONE)
	private boolean placed;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.speed = 0.25F;

		super.<ToggleNode<F, S>>require("knob", "give it one with knob(...)", node -> node.knob != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		final float target = super.isToggle() ? 1F : 0F;
		this.knobProgress = this.placed && this.speed < 1F ? (float) super.getUi().lerpByFramerate(this.knobProgress, target, this.speed, 0.001D) : target;
		this.placed = true;
		this.knob.fit(super.getWidth() / 2D, super.getHeight());
		this.knob.layoutPosition((super.getWidth() - this.knob.getWidth()) * this.knobProgress, (super.getHeight() - this.knob.getHeight()) / 2D);
	}

	@Override
	public void detach() {
		super.detach();
		this.placed = false;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track.zindex(-1));
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T knob(final @NonNull Node knob) {
		this.knob = super.part(this.knob, knob);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T speed(final float speed) {
		return this.speed(Signal.from(speed));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T speed(final @NonNull Supplier<Float> speed) {
		return super.follow("speed", speed, value -> this.speed = value);
	}

}