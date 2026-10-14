package dev.joid.lib.ui.node.effect.impl;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.CircleShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CircleNodeEffect extends NodeEffect<Node> {

	protected CircleNodeEffect() {
		super(100);
	}

	public static @NonNull CircleNodeEffect create() {
		return new CircleNodeEffect();
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return new CircleShaderPass((float) Math.min(node.dw(2D), node.dh(2D)), (float) node.ax(node.dw(2D)), (float) node.ay(node.dh(2D)));
	}

}