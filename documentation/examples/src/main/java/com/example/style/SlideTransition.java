package com.example.style;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;

public class SlideTransition extends Transition {

	protected SlideTransition() {
		super(
			TransitionStep
			.create(500F, TweenEquations.BACK_OUT)
			.draw((ui, progress, body) -> SlideTransition.slide(1F - progress, body)),
			TransitionStep
			.create(300F, TweenEquations.QUART_IN)
			.draw((ui, progress, body) -> SlideTransition.slide(progress, body))
		);
	}

	public static SlideTransition create() {
		return new SlideTransition();
	}

	private static void slide(final float away, final Runnable body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(0D, 300D * away, 0D);
			body.run();
		});
	}

}