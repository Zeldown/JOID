package be.zeldown.joid.lib.ui.node.impl.design.model;

import be.zeldown.joid.lib.utils.context.InternalContext;
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

		if(this.dragged) {
			this.targetRotationYaw += (mouseX - this.draggedMouseX)/5F;
			this.draggedMouseX = mouseX;

			this.targetRotationPitch += (mouseY - this.draggedMouseY)/5F;
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
			super.size(super.getUi().lerpByFramerate(this.getSize(), this.targetSize, 0.1D, 0.0D, true));
		}

		super.draw(mouseX, mouseY);
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (!super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.zoom(this.targetSize + value / 3000D));
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		if (!super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			this.dragged = true;
			this.draggedMouseX = mouseX;
			this.draggedMouseY = mouseY;
		});
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		this.dragged = false;
	}

	@Override
	public <T extends ModelNode> @NonNull T size(final double size) {
		this.targetSize = size;
		return super.size(size);
	}

	@Override
	public <T extends ModelNode> @NonNull T rotationYaw(final double rotationYaw) {
		this.targetRotationYaw = rotationYaw;
		return super.rotationYaw(rotationYaw);
	}

	@Override
	public <T extends ModelNode> @NonNull T rotationPitch(final double rotationPitch) {
		this.targetRotationPitch = rotationPitch;
		return super.rotationPitch(rotationPitch);
	}

	public <T extends ModelViewerNode> @NonNull T rotationYawRange(final double min, final double max) {
		this.minRotationYaw = min;
		this.maxRotationYaw = max;
		return (T) this;
	}

	public <T extends ModelViewerNode> @NonNull T rotationPitchRange(final double min, final double max) {
		this.minRotationPitch = min;
		this.maxRotationPitch = max;
		return (T) this;
	}

	public <T extends ModelViewerNode> @NonNull T sizeRange(final double min, final double max) {
		this.minSize = min;
		this.maxSize = max;
		return (T) this;
	}

	public <T extends ModelViewerNode> @NonNull T zoom(final double zoom) {
		this.targetSize = Math.max(this.minSize, Math.min(this.maxSize, zoom));
		return (T) this;
	}

}
