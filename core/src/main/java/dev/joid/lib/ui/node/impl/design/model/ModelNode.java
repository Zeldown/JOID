package dev.joid.lib.ui.node.impl.design.model;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ModelNode extends BaseModelNode {

	private double zoom;
	private double rotationYaw;
	private double rotationPitch;

	protected ModelNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.zoom          = 1D;
		this.rotationYaw   = 0D;
		this.rotationPitch = 0D;
	}

	public static @NonNull ModelNode create(final double x, final double y, final double width, final double height) {
		return new ModelNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		super.drawModel(this.zoom, this.rotationYaw, this.rotationPitch);
	}

	public final <T extends ModelNode> @NonNull T zoom(final double zoom) {
		return this.zoom(Signal.from(zoom));
	}

	public final <T extends ModelNode> @NonNull T zoom(final @NonNull Supplier<Double> zoom) {
		return super.follow("zoom", zoom, value -> this.zoom = value);
	}

	public final <T extends ModelNode> @NonNull T rotationYaw(final double rotationYaw) {
		return this.rotationYaw(Signal.from(rotationYaw));
	}

	public final <T extends ModelNode> @NonNull T rotationYaw(final @NonNull Supplier<Double> rotationYaw) {
		return super.follow("rotationYaw", rotationYaw, value -> this.rotationYaw = value);
	}

	public final <T extends ModelNode> @NonNull T rotationPitch(final double rotationPitch) {
		return this.rotationPitch(Signal.from(rotationPitch));
	}

	public final <T extends ModelNode> @NonNull T rotationPitch(final @NonNull Supplier<Double> rotationPitch) {
		return super.follow("rotationPitch", rotationPitch, value -> this.rotationPitch = value);
	}

}