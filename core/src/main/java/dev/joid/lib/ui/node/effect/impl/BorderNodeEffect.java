package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.pass.BorderShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class BorderNodeEffect<T extends Node> extends NodeEffect<T, BorderNodeEffect<T>> {

	private boolean fill = true;
	private BorderMode mode;
	private Supplier<Color> colorSupplier;
	private Supplier<Float> widthSupplier;

	private BorderNodeEffect(final @NonNull Color color, final float width, final @NonNull BorderMode mode) {
		this.colorSupplier = () -> color;
		this.widthSupplier = () -> width;
		this.mode = mode;
	}

	public static <T extends Node> @NonNull BorderNodeEffect<T> create(final @NonNull Color color, final float width) {
		return new BorderNodeEffect<>(color, width, BorderMode.OUT);
	}

	public static <T extends Node> @NonNull BorderNodeEffect<T> create(final @NonNull Color color, final float width, final @NonNull BorderMode mode) {
		return new BorderNodeEffect<>(color, width, mode);
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull T node) {
		return new BorderShaderPass(this.widthSupplier.get(), this.colorSupplier.get(), this.fill, this.mode);
	}

	public @NonNull BorderNodeEffect<T> color(final @NonNull Color color) {
		this.colorSupplier = () -> color;
		return this;
	}

	public @NonNull BorderNodeEffect<T> color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorSupplier = colorSupplier;
		return this;
	}

	public @NonNull BorderNodeEffect<T> width(final float width) {
		this.widthSupplier = () -> width;
		return this;
	}

	public @NonNull BorderNodeEffect<T> width(final @NonNull Supplier<Float> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return this;
	}

	public @NonNull BorderNodeEffect<T> mode(final @NonNull BorderMode mode) {
		this.mode = mode;
		return this;
	}

	public @NonNull BorderNodeEffect<T> fill(final boolean fill) {
		this.fill = fill;
		return this;
	}

}