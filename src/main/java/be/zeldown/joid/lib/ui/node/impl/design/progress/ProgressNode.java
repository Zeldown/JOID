package be.zeldown.joid.lib.ui.node.impl.design.progress;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ProgressNode extends Node {

	private float progress;
	private ProgressDirection direction;

	private Color[] colors;
	private Resource[] resources;

	protected ProgressNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.progress  = 0.0F;
		this.direction = ProgressDirection.LEFT_TO_RIGHT;
		this.colors    = new Color[] {Color.BLACK, Color.WHITE};
		this.resources = new Resource[] {null, null};
	}

	public static @NonNull ProgressNode create(final double x, final double y, final double width, final double height) {
		return new ProgressNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.resources[0] != null && this.resources[1] != null) {
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resources[0]);
			super.getUi().stencil(this.getProgressX(), this.getProgressY(), this.getProgressWidth(), this.getProgressHeight(), () -> {
				DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resources[1]);
			});
		} else {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.colors[0]);
			DrawUtils.SHAPE.drawRect(this.getProgressX(), this.getProgressY(), this.getProgressWidth(), this.getProgressHeight(), this.colors[1]);
		}
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

	public <T extends ProgressNode> @NonNull T progress(final float progress) {
		this.progress = progress;
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T direction(final @NonNull ProgressDirection direction) {
		this.direction = direction;
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T progress(final float min, final float max, final float value) {
		this.progress = (value - min) / (max - min);
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T color(final @NonNull Color background, final @NonNull Color foreground) {
		this.colors = new Color[] {background, foreground};
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T background(final @NonNull Color color) {
		this.colors[0] = color;
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T foreground(final @NonNull Color color) {
		this.colors[1] = color;
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T resource(final @NonNull Resource background, final @NonNull Resource foreground) {
		this.resources = new Resource[] { background, foreground };
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T background(final @NonNull Resource resource) {
		this.resources[0] = resource;
		return (T) this;
	}

	public <T extends ProgressNode> @NonNull T foreground(final @NonNull Resource resource) {
		this.resources[1] = resource;
		return (T) this;
	}

	public static enum ProgressDirection {

		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT,
		TOP_TO_BOTTOM,
		BOTTOM_TO_TOP;

	}

}