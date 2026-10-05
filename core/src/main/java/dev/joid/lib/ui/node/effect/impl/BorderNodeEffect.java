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
@SuppressWarnings("unchecked")
public class BorderNodeEffect<T extends Node> extends NodeEffect<T> {

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

	public <E extends BorderNodeEffect<T>> @NonNull E color(final @NonNull Color color) {
		this.colorSupplier = () -> color;
		return (E) this;
	}

	public <E extends BorderNodeEffect<T>> @NonNull E color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorSupplier = colorSupplier;
		return (E) this;
	}

	public <E extends BorderNodeEffect<T>> @NonNull E width(final float width) {
		this.widthSupplier = () -> width;
		return (E) this;
	}

	public <E extends BorderNodeEffect<T>> @NonNull E width(final @NonNull Supplier<Float> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return (E) this;
	}

	public <E extends BorderNodeEffect<T>> @NonNull E mode(final @NonNull BorderMode mode) {
		this.mode = mode;
		return (E) this;
	}

	public <E extends BorderNodeEffect<T>> @NonNull E fill(final boolean fill) {
		this.fill = fill;
		return (E) this;
	}

}