package dev.joid.lib.ui.node.impl.progress;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ProgressNode extends Node {

	private float             progress;
	private ProgressDirection direction;

	private Node fill;
	private Node track;

	protected ProgressNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.progress  = 0F;
		this.direction = ProgressDirection.LEFT_TO_RIGHT;

		super.<ProgressNode>require("fill", "give it one with fill(...)", node -> node.fill != null);
	}

	public static @NonNull ProgressNode create(final double x, final double y, final double width, final double height) {
		return new ProgressNode(x, y, width, height);
	}

	@Override
	public void layout() {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		final double shown = Math.min(1D, Math.max(0D, this.progress));
		final double width = this.direction.isHorizontal() ? super.getWidth() * shown : super.getWidth();
		final double height = this.direction.isHorizontal() ? super.getHeight() : super.getHeight() * shown;
		final double x = this.direction == ProgressDirection.RIGHT_TO_LEFT ? super.getWidth() - width : 0D;
		final double y = this.direction == ProgressDirection.BOTTOM_TO_TOP ? super.getHeight() - height : 0D;
		this.fill.layoutPosition(x, y).layoutSize(width, height);
	}

	public final <T extends ProgressNode> @NonNull T progress(final float progress) {
		return this.progress(Signal.from(progress));
	}

	public final <T extends ProgressNode> @NonNull T progress(final @NonNull Supplier<Float> progress) {
		return super.follow("progress", progress, value -> this.progress = value);
	}

	public final <T extends ProgressNode> @NonNull T direction(final @NonNull ProgressDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends ProgressNode> @NonNull T direction(final @NonNull Supplier<@NonNull ProgressDirection> direction) {
		return super.follow("direction", direction, value -> this.direction = value);
	}

	public final <T extends ProgressNode> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track.zindex(-1));
		return (T) this;
	}

	public final <T extends ProgressNode> @NonNull T fill(final @NonNull Node fill) {
		this.fill = super.part(this.fill, fill);
		return (T) this;
	}

	public static enum ProgressDirection {

		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT,
		TOP_TO_BOTTOM,
		BOTTOM_TO_TOP;

		public boolean isHorizontal() {
			return this == ProgressDirection.LEFT_TO_RIGHT || this == ProgressDirection.RIGHT_TO_LEFT;
		}

	}

}