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
import lombok.NonNull;

public class BorderNodeEffect extends NodeEffect<Node> {

	private Supplier<Float>       widthSupplier;
	private Supplier<Boolean>     cornersSupplier;
	private Supplier<BorderMode>  modeSupplier;
	private Function<Node, Color> colorFunction;

	protected BorderNodeEffect(final @NonNull Supplier<@NonNull Color> colorSupplier, final @NonNull Supplier<Float> widthSupplier) {
		super(200);
		this.modeSupplier    = () -> BorderMode.OUT;
		this.colorFunction   = node -> colorSupplier.get();
		this.widthSupplier   = widthSupplier;
		this.cornersSupplier = () -> true;
	}

	public static @NonNull BorderNodeEffect create(final @NonNull Color color, final float width) {
		return new BorderNodeEffect(Signal.from(color), Signal.from(width));
	}

	public float getWidth() {
		return this.widthSupplier.get();
	}

	public @NonNull BorderMode getMode() {
		return this.modeSupplier.get();
	}

	public @NonNull Color getColor(final @NonNull Node node) {
		return this.colorFunction.apply(node);
	}

	public boolean isCorners() {
		return this.cornersSupplier.get();
	}

	@Override
	public boolean shouldApply(final @NonNull Node node) {
		return this.widthSupplier.get() > 0F;
	}

	@Override
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return BorderShaderPass.create(this.colorFunction.apply(node), this.widthSupplier.get()).corners(this.cornersSupplier.get()).mode(this.modeSupplier.get());
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

	public final <E extends BorderNodeEffect> @NonNull E corners(final boolean corners) {
		return this.corners(Signal.from(corners));
	}

	public final <E extends BorderNodeEffect> @NonNull E corners(final @NonNull Supplier<Boolean> cornersSupplier) {
		this.cornersSupplier = cornersSupplier;
		return (E) this;
	}

}