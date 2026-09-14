package be.zeldown.joid.lib.render.transform.operation;

import java.util.function.Supplier;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.render.modifier.Rotation;
import be.zeldown.joid.lib.render.modifier.Vector;
import lombok.NonNull;

public class RotateOperation implements TransformOperation {

	private final Supplier<Double> angleSupplier;
	private final Rotation         rotation;
	private final Vector           pivot;

	public RotateOperation(final double angle, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
		this(() -> angle, rotation, pivot);
	}

	public RotateOperation(final @NonNull Supplier<Double> angleSupplier, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
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
		final IRenderBridge render = BridgeHandler.getRender();
		render.translate(pivotX, pivotY, pivotZ);
		render.rotate(this.angleSupplier.get(), x, y, z);
		render.translate(-pivotX, -pivotY, -pivotZ);
	}

	@Override
	public void reset() {
		final double x = this.rotation.getRawX();
		final double y = this.rotation.getRawY();
		final double z = this.rotation.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		final IRenderBridge render = BridgeHandler.getRender();
		render.translate(pivotX, pivotY, pivotZ);
		render.rotate(-this.angleSupplier.get(), x, y, z);
		render.translate(-pivotX, -pivotY, -pivotZ);
	}

}