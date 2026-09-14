package be.zeldown.joid.lib.ui.node.effect.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.BlurShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class BlurNodeEffect<T extends Node> extends NodeEffect<T> {

	private Supplier<Float> radiusSupplier;

	private BlurNodeEffect(final float radius) {
		this.radiusSupplier = () -> radius;
	}

	public static <T extends Node> @NonNull BlurNodeEffect<T> create(final float radius) {
		return new BlurNodeEffect<>(radius);
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public List<ShaderPass> toShaderPasses(final @NonNull T node) {
		final float radius = this.radiusSupplier.get();
		return Arrays.asList(new BlurShaderPass(radius, true, 0), new BlurShaderPass(radius, false, 0));
	}

	/* [ Setter Section ] */
	public <E extends BlurNodeEffect<T>> @NonNull E radius(final float radius) {
		this.radiusSupplier = () -> radius;
		return (E) this;
	}

	public <E extends BlurNodeEffect<T>> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

}