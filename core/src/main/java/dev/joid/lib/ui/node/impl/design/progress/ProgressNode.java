package dev.joid.lib.ui.node.impl.design.progress;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ProgressNode extends Node {

	private float             progress;
	private ProgressDirection direction;

	private Color background;
	private Color foreground;

	private Resource backgroundResource;
	private Resource foregroundResource;

	protected ProgressNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.progress   = 0F;
		this.direction  = ProgressDirection.LEFT_TO_RIGHT;
		this.background = Color.BLACK;
		this.foreground = Color.WHITE;
	}

	public static @NonNull ProgressNode create(final double x, final double y, final double width, final double height) {
		return new ProgressNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.backgroundResource != null && this.foregroundResource != null) {
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.backgroundResource);
			super.getUi().mask(this.getProgressX(), this.getProgressY(), this.getProgressWidth(), this.getProgressHeight(), () -> {
				DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.foregroundResource);
			});
		} else {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.background);
			DrawUtils.SHAPE.drawRect(this.getProgressX(), this.getProgressY(), this.getProgressWidth(), this.getProgressHeight(), this.foreground);
		}
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

	public final <T extends ProgressNode> @NonNull T background(final @NonNull Color background) {
		return this.background(Signal.from(background));
	}

	public final <T extends ProgressNode> @NonNull T background(final @NonNull Supplier<@NonNull Color> background) {
		return super.follow("background", background, value -> this.background = value);
	}

	public final <T extends ProgressNode> @NonNull T foreground(final @NonNull Color foreground) {
		return this.foreground(Signal.from(foreground));
	}

	public final <T extends ProgressNode> @NonNull T foreground(final @NonNull Supplier<@NonNull Color> foreground) {
		return super.follow("foreground", foreground, value -> this.foreground = value);
	}

	public final <T extends ProgressNode> @NonNull T backgroundResource(final Resource backgroundResource) {
		return this.backgroundResource(Signal.from(backgroundResource));
	}

	public final <T extends ProgressNode> @NonNull T backgroundResource(final @NonNull Supplier<Resource> backgroundResource) {
		return super.follow("backgroundResource", backgroundResource, value -> this.backgroundResource = value);
	}

	public final <T extends ProgressNode> @NonNull T foregroundResource(final Resource foregroundResource) {
		return this.foregroundResource(Signal.from(foregroundResource));
	}

	public final <T extends ProgressNode> @NonNull T foregroundResource(final @NonNull Supplier<Resource> foregroundResource) {
		return super.follow("foregroundResource", foregroundResource, value -> this.foregroundResource = value);
	}

	private double getProgressX() {
		switch (this.direction) {
		case RIGHT_TO_LEFT:
			return super.getX() + super.getWidth() * (1 - this.progress);
		default:
			return super.getX();
		}
	}

	private double getProgressY() {
		switch (this.direction) {
		case BOTTOM_TO_TOP:
			return super.getY() + super.getHeight() * (1 - this.progress);
		default:
			return super.getY();
		}
	}

	private double getProgressWidth() {
		switch (this.direction) {
		case LEFT_TO_RIGHT:
			return super.getWidth() * this.progress;
		case RIGHT_TO_LEFT:
			return super.getWidth() * this.progress;
		default:
			return super.getWidth();
		}
	}

	private double getProgressHeight() {
		switch (this.direction) {
		case TOP_TO_BOTTOM:
			return super.getHeight() * this.progress;
		case BOTTOM_TO_TOP:
			return super.getHeight() * this.progress;
		default:
			return super.getHeight();
		}
	}

	public static enum ProgressDirection {

		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT,
		TOP_TO_BOTTOM,
		BOTTOM_TO_TOP;

	}

}