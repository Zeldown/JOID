package dev.joid.lib.ui.node.effect.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.pass.BlurShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class BlurNodeEffect extends NodeEffect<Node> {

	private Supplier<Float> radiusSupplier;

	protected BlurNodeEffect(final float radius) {
		this.radiusSupplier = () -> radius;
	}

	public static @NonNull BlurNodeEffect create(final float radius) {
		return new BlurNodeEffect(radius);
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public List<ShaderPass> toShaderPasses(final @NonNull Node node) {
		final float radius = this.radiusSupplier.get();
		if (radius <= 0F) {
			return Collections.emptyList();
		}

		return Arrays.asList(new BlurShaderPass(radius, true, 0), new BlurShaderPass(radius, false, 0));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final float radius) {
		return this.radius(Signal.from(radius));
	}

	public final <E extends BlurNodeEffect> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

}