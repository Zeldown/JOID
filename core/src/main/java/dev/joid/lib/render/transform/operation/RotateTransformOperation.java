package dev.joid.lib.render.transform.operation;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Vector;
import lombok.NonNull;

public class RotateTransformOperation implements ITransformOperation {

	private final Vector           pivot;
	private final Rotation         rotation;
	private final Supplier<Double> angleSupplier;

	public RotateTransformOperation(final double angle, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
		this(() -> angle, rotation, pivot);
	}

	public RotateTransformOperation(final @NonNull Supplier<Double> angleSupplier, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
		this.angleSupplier = angleSupplier;
		this.rotation = rotation;
		this.pivot = pivot;
	}

	@Override
	public void transform() {
		final double x = this.rotation.getRawX();
		final double y = this.rotation.getRawY();
		final double z = this.rotation.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.translate(pivotX, pivotY, pivotZ);
		render.rotate(this.angleSupplier.get(), x, y, z);
		render.translate(-pivotX, -pivotY, -pivotZ);
	}

}