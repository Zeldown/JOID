package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.operation.ITransformOperation;
import dev.joid.lib.render.transform.operation.RotateTransformOperation;
import dev.joid.lib.render.transform.operation.ScaleTransformOperation;
import dev.joid.lib.render.transform.operation.TranslateTransformOperation;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("unchecked")
public class TransformNodeEffect extends NodeEffect<Node> {

	private Supplier<Transformation> transformationSupplier;

	protected TransformNodeEffect(final Transformation transformation) {
		this.transformationSupplier = () -> transformation;
	}

	public static TransformNodeEffect create(final @NonNull ScaleTransformOperation scale) {
		return new TransformNodeEffect(Transformation.create(scale));
	}

	public static TransformNodeEffect create(final @NonNull RotateTransformOperation rotation) {
		return new TransformNodeEffect(Transformation.create(rotation));
	}

	public static TransformNodeEffect create(final @NonNull TranslateTransformOperation translate) {
		return new TransformNodeEffect(Transformation.create(translate));
	}

	public static TransformNodeEffect create(final @NonNull Transformation transformation) {
		return new TransformNodeEffect(transformation);
	}

	@Override
	public void pre(final @NonNull Node node, final double mouseX, final double mouseY) {
		BridgeHandler.RENDER.get().pushMatrix();
		for (final ITransformOperation operation : this.transformationSupplier.get().getOperations()) {
			operation.transform();
		}
	}

	@Override
	public void post(final @NonNull Node node, final double mouseX, final double mouseY) {
		BridgeHandler.RENDER.get().popMatrix();
	}

	public final <E extends TransformNodeEffect> @NonNull E transformation(final @NonNull Transformation transformation) {
		return this.transformation(Signal.from(transformation));
	}

	public final <E extends TransformNodeEffect> @NonNull E transformation(final @NonNull Supplier<Transformation> transformationSupplier) {
		this.transformationSupplier = transformationSupplier;
		return (E) this;
	}

}