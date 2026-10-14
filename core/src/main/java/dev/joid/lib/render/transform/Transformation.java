package dev.joid.lib.render.transform;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.render.Drawing;
import lombok.NonNull;

public class Transformation {

	private final List<Operation> operations;

	private Transformation() {
		this.operations = new LinkedList<>();
	}

	public static @NonNull Transformation create() {
		return new Transformation();
	}

	public @NonNull Transformation translate(final @NonNull Vector vector) {
		this.operations.add((render, pivotX, pivotY) -> {
			final double x = vector.getX();
			final double y = vector.getY();
			render.getModelView().translate(x, y, vector.getZ());
			render.quantize(x, y);
		});
		return this;
	}

	public @NonNull Transformation rotate(final double angle, final @NonNull Rotation rotation) {
		return this.rotate(() -> angle, rotation);
	}

	public @NonNull Transformation rotate(final @NonNull Supplier<Double> angle, final @NonNull Rotation rotation) {
		return this.rotate(angle, rotation, null);
	}

	public @NonNull Transformation rotate(final double angle, final @NonNull Rotation rotation, final Vector pivot) {
		return this.rotate(() -> angle, rotation, pivot);
	}

	public @NonNull Transformation rotate(final @NonNull Supplier<Double> angle, final @NonNull Rotation rotation, final Vector pivot) {
		this.operations.add((render, pivotX, pivotY) -> Transformation.around(render, pivot, pivotX, pivotY, () -> render.getModelView().rotate(angle.get(), rotation.getPitch(), rotation.getYaw(), rotation.getRoll())));
		return this;
	}

	public @NonNull Transformation scale(final @NonNull Scale scale) {
		return this.scale(scale, null);
	}

	public @NonNull Transformation scale(final @NonNull Scale scale, final Vector pivot) {
		this.operations.add((render, pivotX, pivotY) -> Transformation.around(render, pivot, pivotX, pivotY, () -> render.getModelView().scale(scale.getRawX(), scale.getRawY(), scale.getRawZ())));
		return this;
	}

	public void apply() {
		BridgeHandler.RENDER.get().getModelView().push();
		this.transform(0D, 0D);
	}

	public void apply(final @NonNull Drawing drawing) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			this.transform(0D, 0D);
			drawing.draw();
		});
	}

	public void reset() {
		BridgeHandler.RENDER.get().getModelView().pop();
	}

	public void transform(final double pivotX, final double pivotY) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		for (final Operation operation : this.operations) {
			operation.transform(render, pivotX, pivotY);
		}
	}

	public void clear() {
		this.operations.clear();
	}

	private static void around(final IRenderBridge render, final Vector pivot, final double pivotX, final double pivotY, final Runnable transform) {
		final double x = pivot != null ? pivot.getX() : pivotX;
		final double y = pivot != null ? pivot.getY() : pivotY;
		final double z = pivot != null ? pivot.getZ() : 0D;
		render.getModelView().translate(x, y, z);
		transform.run();
		render.getModelView().translate(-x, -y, -z);
	}

	private interface Operation {

		public void transform(final IRenderBridge render, final double pivotX, final double pivotY);

	}

}