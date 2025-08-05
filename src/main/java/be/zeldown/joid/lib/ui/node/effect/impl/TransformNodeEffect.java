package be.zeldown.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.transform.GLTransformation;
import be.zeldown.joid.lib.opengl.transform.glto.GLTO;
import be.zeldown.joid.lib.opengl.transform.glto.GLTORotating;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOScaling;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOTranslating;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformNodeEffect<T extends Node> extends NodeEffect<T> {

	private Supplier<GLTransformation> transformationSupplier;

	private TransformNodeEffect(final GLTransformation transformation) {
		this.transformationSupplier = () -> transformation;
	}

	public static <T extends Node> TransformNodeEffect<T> create(final @NonNull GLTransformation transformation) {
		return new TransformNodeEffect<>(transformation);
	}

	public static <T extends Node> TransformNodeEffect<T> create(final @NonNull GLTOScaling scale) {
		return new TransformNodeEffect<>(GLTransformation.create(scale));
	}

	public static <T extends Node> TransformNodeEffect<T> create(final @NonNull GLTORotating rotation) {
		return new TransformNodeEffect<>(GLTransformation.create(rotation));
	}

	public static <T extends Node> TransformNodeEffect<T> create(final @NonNull GLTOTranslating translate) {
		return new TransformNodeEffect<>(GLTransformation.create(translate));
	}

	/* [ Internal Section ] */
	@Override
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {
		GL11.glPushMatrix();
		for (final GLTO operation : this.transformationSupplier.get().getOperations()) {
			operation.transform();
		}
	}

	@Override
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {
		GL11.glPopMatrix();
	}

	/* [ Setter Section ] */
	public <E extends TransformNodeEffect<T>> @NonNull E transformation(final @NonNull GLTransformation transformation) {
		this.transformationSupplier = () -> transformation;
		return (E) this;
	}

	public <E extends TransformNodeEffect<T>> @NonNull E transformation(final @NonNull Supplier<GLTransformation> transformationSupplier) {
		this.transformationSupplier = transformationSupplier;
		return (E) this;
	}

}