package dev.joid.lib.render.transform;

import java.util.LinkedList;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.render.transform.operation.ITransformOperation;
import dev.joid.lib.render.transform.operation.RotateTransformOperation;
import dev.joid.lib.render.transform.operation.ScaleTransformOperation;
import dev.joid.lib.render.transform.operation.TranslateTransformOperation;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Transformation {

	@NonNull
	private final List<@NonNull ITransformOperation> operations;

	private Transformation() {
		this.operations = new LinkedList<>();
	}

	public static @NonNull Transformation create() {
		return new Transformation();
	}

	public static @NonNull Transformation create(final @NonNull ITransformOperation operation) {
		return new Transformation().add(operation);
	}

	public @NonNull Transformation add(final @NonNull ITransformOperation operation) {
		this.operations.add(operation);
		return this;
	}

	public @NonNull Transformation translate(final @NonNull Vector vector) {
		this.operations.add(new TranslateTransformOperation(vector));
		return this;
	}

	public @NonNull Transformation rotate(final double angle, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
		this.operations.add(new RotateTransformOperation(angle, rotation, pivot));
		return this;
	}

	public @NonNull Transformation scale(final @NonNull Scale scale, final @NonNull Vector pivot) {
		this.operations.add(new ScaleTransformOperation(scale, pivot));
		return this;
	}

	public void apply() {
		BridgeHandler.RENDER.get().pushMatrix();
		this.operations.forEach(ITransformOperation::transform);
	}

	public void apply(final @NonNull Drawing drawing) {
		this.apply();
		try {
			drawing.draw();
		} finally {
			this.reset();
		}
	}

	public void reset() {
		BridgeHandler.RENDER.get().popMatrix();
	}

	public void clear() {
		this.operations.clear();
	}

}