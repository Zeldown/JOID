package dev.joid.lib.ui.core.transition.impl;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;
import dev.joid.lib.ui.core.view.UIView;

public class PopTransition extends Transition {

	public PopTransition() {
		super(TransitionStep.create(130F, TweenEquations.QUART_OUT).draw(PopTransition::pop), TransitionStep.create(130F, TweenEquations.QUART_IN).draw((ui, progress, body) -> PopTransition.pop(ui, 1F - progress, body)));
	}

	private static void pop(final UI ui, final float shown, final Drawing body) {
		final double scale = 0.75D + shown * 0.25D;
		final UIView view = ui.getView();
		final double pivotX = view.getOffsetX() + view.getAnchorX();
		final double pivotY = view.getOffsetY() + view.getAnchorY();
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(pivotX, pivotY, 0);
			matrix.scale(scale, scale, 1D);
			matrix.translate(-pivotX, -pivotY, 0);
			body.draw();
		});
	}

}