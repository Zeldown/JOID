package dev.joid.lib.ui.node.effect.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.BlurShaderPass;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
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

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public List<IShaderPass> toShaderPasses(final @NonNull Node node) {
		final float radius = this.radiusSupplier.get();
		if (radius <= 0F) {
			return Collections.emptyList();
		}

		return Arrays.asList(new BlurShaderPass(radius, true), new BlurShaderPass(radius, false));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final float radius) {
		return this.radius(Signal.from(radius));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

}