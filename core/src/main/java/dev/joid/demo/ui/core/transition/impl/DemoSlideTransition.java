package dev.joid.demo.ui.core.transition.impl;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;

public class DemoSlideTransition extends Transition {

	public DemoSlideTransition() {
		super(TransitionStep.create(500F, TweenEquations.BACK_OUT).draw((ui, progress, body) -> DemoSlideTransition.slide(1080D * (1F - progress), body)), TransitionStep.create(400F, TweenEquations.QUART_IN).draw((ui, progress, body) -> DemoSlideTransition.slide(1080D * progress, body)));
	}

	private static void slide(final double y, final Drawing body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(0D, y, 0D);
			body.draw();
		});
	}

}