package com.example.further;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

// #region effect
public class LiftNodeEffect extends NodeEffect<Node> {

	private final double height;

	protected LiftNodeEffect(final double height) {
		super(-70);
		this.height = height;
	}

	public static LiftNodeEffect create(final double height) {
		return new LiftNodeEffect(height);
	}

	@Override
	public boolean shouldApply(final Node node) {
		return node.isEnabled();
	}

	@Override
	public void draw(final Node node, final Runnable body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(0D, -node.hoverValue((float) this.height), 0D);
			body.run();
		});
	}

}
// #endregion