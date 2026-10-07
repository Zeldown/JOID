package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.pass.BorderShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class BorderNodeEffect extends NodeEffect<Node> {

	private Supplier<Boolean>    fillSupplier;
	private Supplier<BorderMode> modeSupplier;
	private Supplier<Color>      colorSupplier;
	private Supplier<Float>      widthSupplier;

	protected BorderNodeEffect(final @NonNull Color color, final float width, final @NonNull BorderMode mode) {
		this.fillSupplier  = () -> true;
		this.modeSupplier  = () -> mode;
		this.colorSupplier = () -> color;
		this.widthSupplier = () -> width;
	}

	public static @NonNull BorderNodeEffect create(final @NonNull Color color, final float width) {
		return new BorderNodeEffect(color, width, BorderMode.OUT);
	}

	public static @NonNull BorderNodeEffect create(final @NonNull Color color, final float width, final @NonNull BorderMode mode) {
		return new BorderNodeEffect(color, width, mode);
	}

	public boolean isFill() {
		return this.fillSupplier.get();
	}

	public @NonNull BorderMode getMode() {
		return this.modeSupplier.get();
	}

	@Override
	public boolean shouldApply(final @NonNull Node node) {
		return this.widthSupplier.get() > 0F;
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull Node node) {
		return new BorderShaderPass(this.widthSupplier.get(), this.colorSupplier.get(), this.fillSupplier.get(), this.modeSupplier.get());
	}

	public final <E extends BorderNodeEffect> @NonNull E color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <E extends BorderNodeEffect> @NonNull E color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorSupplier = colorSupplier;
		return (E) this;
	}

	public final <E extends BorderNodeEffect> @NonNull E width(final float width) {
		return this.width(Signal.from(width));
	}

	public final <E extends BorderNodeEffect> @NonNull E width(final @NonNull Supplier<Float> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return (E) this;
	}

	public final <E extends BorderNodeEffect> @NonNull E mode(final @NonNull BorderMode mode) {
		return this.mode(Signal.from(mode));
	}

	public final <E extends BorderNodeEffect> @NonNull E mode(final @NonNull Supplier<@NonNull BorderMode> modeSupplier) {
		this.modeSupplier = modeSupplier;
		return (E) this;
	}

	public final <E extends BorderNodeEffect> @NonNull E fill(final boolean fill) {
		return this.fill(Signal.from(fill));
	}

	public final <E extends BorderNodeEffect> @NonNull E fill(final @NonNull Supplier<Boolean> fillSupplier) {
		this.fillSupplier = fillSupplier;
		return (E) this;
	}

}