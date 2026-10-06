package dev.joid.lib.ui.node.impl.design.model;

import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ModelViewerNode extends ModelNode {

	private boolean dragged;
	private double  draggedMouseX;
	private double  draggedMouseY;

	private double targetSize;
	private double targetRotationYaw;
	private double targetRotationPitch;

	private double lastSize;
	private double lastRotationYaw;
	private double lastRotationPitch;

	private double minSize;
	private double maxSize;

	private double minRotationYaw;
	private double maxRotationYaw;

	private double minRotationPitch;
	private double maxRotationPitch;

	protected ModelViewerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.targetSize = super.getSize();
		this.targetRotationYaw = super.getRotationYaw();
		this.targetRotationPitch = super.getRotationPitch();

		this.lastSize = super.getSize();
		this.lastRotationYaw = super.getRotationYaw();
		this.lastRotationPitch = super.getRotationPitch();

		this.minSize = 0.1D;
		this.maxSize = 2D;

		this.minRotationYaw = -Double.MAX_VALUE;
		this.maxRotationYaw = Double.MAX_VALUE;

		this.minRotationPitch = -Double.MAX_VALUE;
		this.maxRotationPitch = Double.MAX_VALUE;
	}

	public static @NonNull ModelViewerNode create(final double x, final double y, final double width, final double height) {
		return new ModelViewerNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (super.getModel() == null) {
			return;
		}

		this.sync();

		if(this.dragged) {
			this.targetRotationYaw += (mouseX - this.draggedMouseX) / 5F;
			this.draggedMouseX = mouseX;

			this.targetRotationPitch -= (mouseY - this.draggedMouseY) / 5F;
			this.draggedMouseY = mouseY;

			this.targetRotationYaw = Math.max(this.minRotationYaw, Math.min(this.maxRotationYaw, this.targetRotationYaw));
			this.targetRotationPitch = Math.max(this.minRotationPitch, Math.min(this.maxRotationPitch, this.targetRotationPitch));
		}

		if(super.getRotationYaw() != this.targetRotationYaw) {
			super.rotationYaw(super.getUi().lerpByFramerate(super.getRotationYaw(), this.targetRotationYaw, 0.1D, 0.1D, true));
		}

		if(super.getRotationPitch() != this.targetRotationPitch) {
			super.rotationPitch(super.getUi().lerpByFramerate(this.getRotationPitch(), this.targetRotationPitch, 0.1D, 0.1D, true));
		}

		if(super.getSize() != this.targetSize) {
			super.size(super.getUi().lerpByFramerate(this.getSize(), this.targetSize, 0.1D, 0D, true));
		}

		this.lastSize = super.getSize();
		this.lastRotationYaw = super.getRotationYaw();
		this.lastRotationPitch = super.getRotationPitch();

		super.draw(mouseX, mouseY);
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (!super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.zoom(this.getTargetSize() + value / 3000D));
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (!clickType.isLeft() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			this.dragged = true;
			this.draggedMouseX = mouseX;
			this.draggedMouseY = mouseY;
		});
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (clickType.isLeft()) {
			this.dragged = false;
		}
	}

	public final double getTargetSize() {
		return super.getSize() != this.lastSize ? super.getSize() : this.targetSize;
	}

	public final double getTargetRotationYaw() {
		return super.getRotationYaw() != this.lastRotationYaw ? super.getRotationYaw() : this.targetRotationYaw;
	}

	public final double getTargetRotationPitch() {
		return super.getRotationPitch() != this.lastRotationPitch ? super.getRotationPitch() : this.targetRotationPitch;
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final double min, final double max) {
		this.minRotationYaw = min;
		this.maxRotationYaw = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final double min, final double max) {
		this.minRotationPitch = min;
		this.maxRotationPitch = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T sizeRange(final double min, final double max) {
		this.minSize = min;
		this.maxSize = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final double zoom) {
		this.sync();
		this.targetSize = Math.max(this.minSize, Math.min(this.maxSize, zoom));
		return (T) this;
	}

	private void sync() {
		if (super.getSize() != this.lastSize) {
			this.targetSize = super.getSize();
			this.lastSize = super.getSize();
		}

		if (super.getRotationYaw() != this.lastRotationYaw) {
			this.targetRotationYaw = super.getRotationYaw();
			this.lastRotationYaw = super.getRotationYaw();
		}

		if (super.getRotationPitch() != this.lastRotationPitch) {
			this.targetRotationPitch = super.getRotationPitch();
			this.lastRotationPitch = super.getRotationPitch();
		}
	}

}