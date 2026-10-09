package dev.joid.lib.render.transform;

import java.util.LinkedList;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.render.transform.operation.RotateOperation;
import dev.joid.lib.render.transform.operation.ScaleOperation;
import dev.joid.lib.render.transform.operation.TransformOperation;
import dev.joid.lib.render.transform.operation.TranslateOperation;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Transformation {

	@NonNull
	private final List<@NonNull TransformOperation> operations;

	private Transformation() {
		this.operations = new LinkedList<>();
	}

	public static @NonNull Transformation create() {
		return new Transformation();
	}

	public static @NonNull Transformation create(final @NonNull TransformOperation operation) {
		return new Transformation().add(operation);
	}

	public @NonNull Transformation add(final @NonNull TransformOperation operation) {
		this.operations.add(operation);
		return this;
	}

	public @NonNull Transformation translate(final @NonNull Vector vector) {
		this.operations.add(new TranslateOperation(vector));
		return this;
	}

	public @NonNull Transformation rotate(final double angle, final @NonNull Rotation rotation, final @NonNull Vector pivot) {
		this.operations.add(new RotateOperation(angle, rotation, pivot));
		return this;
	}

	public @NonNull Transformation scale(final @NonNull Scale scale, final @NonNull Vector pivot) {
		this.operations.add(new ScaleOperation(scale, pivot));
		return this;
	}

	public void apply() {
		BridgeHandler.RENDER.get().pushMatrix();
		this.operations.forEach(TransformOperation::transform);
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