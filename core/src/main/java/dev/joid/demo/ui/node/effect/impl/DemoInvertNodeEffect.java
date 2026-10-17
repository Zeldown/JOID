package dev.joid.demo.ui.node.effect.impl;

import dev.joid.demo.shader.pipeline.pass.DemoInvertShaderPass;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoInvertNodeEffect extends NodeEffect<Node> {

	private final float amount;

	public static @NonNull DemoInvertNodeEffect create(final float amount) {
		return new DemoInvertNodeEffect(amount);
	}

	@Override
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return DemoInvertShaderPass.create(this.amount);
	}

}