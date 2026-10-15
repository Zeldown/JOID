package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.BorderShaderPass;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class BorderNodeEffect extends NodeEffect<Node> {

	private Supplier<Float>       widthSupplier;
	private Supplier<Boolean>     fillSupplier;
	private Supplier<BorderMode>  modeSupplier;
	private Function<Node, Color> colorFunction;

	protected BorderNodeEffect(final @NonNull Color color, final float width) {
		super(200);
		this.fillSupplier  = () -> true;
		this.modeSupplier  = () -> BorderMode.OUT;
		this.colorFunction = node -> color;
		this.widthSupplier = () -> width;
	}

	public static @NonNull BorderNodeEffect create(final @NonNull Color color, final float width) {
		return new BorderNodeEffect(color, width);
	}

	public float getWidth() {
		return this.widthSupplier.get();
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
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return new BorderShaderPass(this.widthSupplier.get(), this.colorFunction.apply(node), this.fillSupplier.get(), this.modeSupplier.get());
	}

	public final <E extends BorderNodeEffect> @NonNull E color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <E extends BorderNodeEffect> @NonNull E color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorFunction = node -> colorSupplier.get();
		return (E) this;
	}

	public final <E extends BorderNodeEffect, N extends Node> @NonNull E color(final @NonNull Function<@NonNull N, @NonNull Color> colorFunction) {
		this.colorFunction = (Function<Node, Color>) colorFunction;
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