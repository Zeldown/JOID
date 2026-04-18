package be.zeldown.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.GradientShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class GradientNodeEffect<T extends Node> extends NodeEffect<T> {

	private Supplier<Color> startColorSupplier;
	private Supplier<Color> endColorSupplier;
	private Vector4f direction;

	private GradientNodeEffect(final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Vector4f direction) {
		this.startColorSupplier = () -> startColor;
		this.endColorSupplier = () -> endColor;
		this.direction = direction;
	}

	public static <T extends Node> @NonNull GradientNodeEffect<T> create(final @NonNull Color startColor, final @NonNull Color endColor) {
		return new GradientNodeEffect<>(startColor, endColor, new Vector4f(0F, 0F, 1F, 0F));
	}

	public static <T extends Node> @NonNull GradientNodeEffect<T> create(final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Vector4f direction) {
		return new GradientNodeEffect<>(startColor, endColor, direction);
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull T node) {
		final Vector4f canvas = new Vector4f((float) node.getX(), (float) node.getY(), (float) (node.getX() + node.getWidth()), (float) (node.getY() + node.getHeight()));
		return new GradientShaderPass(new ColorGradient(this.startColorSupplier.get(), this.endColorSupplier.get(), this.direction), canvas);
	}

	/* [ Setter Section ] */
	public <E extends GradientNodeEffect<T>> @NonNull E startColor(final @NonNull Color color) {
		this.startColorSupplier = () -> color;
		return (E) this;
	}

	public <E extends GradientNodeEffect<T>> @NonNull E endColor(final @NonNull Color color) {
		this.endColorSupplier = () -> color;
		return (E) this;
	}

	public <E extends GradientNodeEffect<T>> @NonNull E direction(final @NonNull Vector4f direction) {
		this.direction = direction;
		return (E) this;
	}

}