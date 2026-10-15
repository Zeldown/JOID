package dev.joid.demo.ui.core.transition;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;
import lombok.NonNull;

public class DemoSlideTransition extends Transition {

	protected DemoSlideTransition() {
		super(TransitionStep.create(500F, TweenEquations.BACK_OUT).draw((ui, progress, body) -> DemoSlideTransition.slide(1080D * (1F - progress), body)), TransitionStep.create(400F, TweenEquations.QUART_IN).draw((ui, progress, body) -> DemoSlideTransition.slide(1080D * progress, body)));
	}

	public static @NonNull DemoSlideTransition create() {
		return new DemoSlideTransition();
	}

	private static void slide(final double y, final Runnable body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(0D, y, 0D);
			body.run();
		});
	}

}