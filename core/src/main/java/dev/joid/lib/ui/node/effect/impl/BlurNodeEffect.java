package dev.joid.lib.ui.node.effect.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.BlurShaderPass;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.NonNull;

public class BlurNodeEffect extends NodeEffect<Node> {

	private Supplier<Float> radiusSupplier;

	protected BlurNodeEffect(final @NonNull Supplier<Float> radiusSupplier) {
		super(150);
		this.radiusSupplier = radiusSupplier;
	}

	public static @NonNull BlurNodeEffect create(final float radius) {
		return new BlurNodeEffect(() -> radius);
	}

	public static @NonNull BlurNodeEffect create(final @NonNull Supplier<Float> radiusSupplier) {
		return new BlurNodeEffect(radiusSupplier);
	}

	public float getRadius() {
		return this.radiusSupplier.get();
	}

	@Override
	public boolean shouldApply(final @NonNull Node node) {
		return this.radiusSupplier.get() > 0F;
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public List<IShaderPass> toShaderPasses(final @NonNull Node node) {
		final float radius = this.radiusSupplier.get();
		return Arrays.asList(BlurShaderPass.create(radius, true), BlurShaderPass.create(radius, false));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final float radius) {
		return this.radius(Signal.from(radius));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

}