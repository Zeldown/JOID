package dev.joid.lib.ui.node.impl.design.model;

import java.util.function.Supplier;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.callback.DispatchContext;
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

	private Supplier<Double> minSize;
	private Supplier<Double> maxSize;

	private Supplier<Double> minRotationYaw;
	private Supplier<Double> maxRotationYaw;

	private Supplier<Double> minRotationPitch;
	private Supplier<Double> maxRotationPitch;

	protected ModelViewerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.targetSize = super.getSize();
		this.targetRotationYaw = super.getRotationYaw();
		this.targetRotationPitch = super.getRotationPitch();

		this.lastSize = super.getSize();
		this.lastRotationYaw = super.getRotationYaw();
		this.lastRotationPitch = super.getRotationPitch();

		this.minSize = () -> 0.1D;
		this.maxSize = () -> 2D;

		this.minRotationYaw = () -> -Double.MAX_VALUE;
		this.maxRotationYaw = () -> Double.MAX_VALUE;

		this.minRotationPitch = () -> -Double.MAX_VALUE;
		this.maxRotationPitch = () -> Double.MAX_VALUE;
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

			this.targetRotationYaw = Math.max(this.minRotationYaw.get(), Math.min(this.maxRotationYaw.get(), this.targetRotationYaw));
			this.targetRotationPitch = Math.max(this.minRotationPitch.get(), Math.min(this.maxRotationPitch.get(), this.targetRotationPitch));
		}

		final double rotationYaw = super.getRotationYaw() != this.targetRotationYaw ? super.getUi().lerpByFramerate(super.getRotationYaw(), this.targetRotationYaw, 0.1D, 0.1D, true) : super.getRotationYaw();
		final double rotationPitch = super.getRotationPitch() != this.targetRotationPitch ? super.getUi().lerpByFramerate(super.getRotationPitch(), this.targetRotationPitch, 0.1D, 0.1D, true) : super.getRotationPitch();
		final double size = super.getSize() != this.targetSize ? super.getUi().lerpByFramerate(super.getSize(), this.targetSize, 0.1D, 0D, true) : super.getSize();
		super.transform(size, rotationYaw, rotationPitch);

		this.lastSize = super.getSize();
		this.lastRotationYaw = super.getRotationYaw();
		this.lastRotationPitch = super.getRotationPitch();

		super.draw(mouseX, mouseY);
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull DispatchContext context) {
		if (notchesY == 0D || !context.isOnPath(this)) {
			return;
		}

		context.cancel(() -> this.zoom(this.getTargetSize() + notchesY / 25D));
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (!button.isLeft() || !context.isOnPath(this)) {
			return;
		}

		context.cancel(() -> {
			this.dragged = true;
			this.draggedMouseX = mouseX;
			this.draggedMouseY = mouseY;
		});
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (button.isLeft()) {
			this.dragged = false;
		}
	}

	@Override
	public void detach() {
		this.dragged = false;
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

	public final double getMinSize() {
		return this.minSize.get();
	}

	public final double getMaxSize() {
		return this.maxSize.get();
	}

	public final double getMinRotationYaw() {
		return this.minRotationYaw.get();
	}

	public final double getMaxRotationYaw() {
		return this.maxRotationYaw.get();
	}

	public final double getMinRotationPitch() {
		return this.minRotationPitch.get();
	}

	public final double getMaxRotationPitch() {
		return this.maxRotationPitch.get();
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final double min, final double max) {
		this.maxRotationYaw = Signal.from(max);
		this.minRotationYaw = Signal.from(min);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final double min, final double max) {
		this.maxRotationPitch = Signal.from(max);
		this.minRotationPitch = Signal.from(min);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T sizeRange(final double min, final double max) {
		this.maxSize = Signal.from(max);
		this.minSize = Signal.from(min);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final double zoom) {
		this.sync();
		this.targetSize = Math.max(this.minSize.get(), Math.min(this.maxSize.get(), zoom));
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