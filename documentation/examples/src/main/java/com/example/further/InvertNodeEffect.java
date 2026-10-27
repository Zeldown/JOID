package com.example.further;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

// #region effect
public class InvertNodeEffect extends NodeEffect<Node> {

	private final float amount;

	protected InvertNodeEffect(final float amount) {
		super(175);
		this.amount = amount;
	}

	public static InvertNodeEffect create(final float amount) {
		return new InvertNodeEffect(amount);
	}

	@Override
	public IShaderPass toShaderPass(final Node node) {
		return InvertShaderPass.create(this.amount);
	}

}
// #endregion