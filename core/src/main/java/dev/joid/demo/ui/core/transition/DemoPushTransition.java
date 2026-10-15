package dev.joid.demo.ui.core.transition;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;
import lombok.NonNull;

public class DemoPushTransition extends Transition {

	protected DemoPushTransition() {
		super(TransitionStep.create(1000F, TweenEquations.QUART_OUT).draw((ui, progress, body) -> DemoPushTransition.push(1920D * (1F - progress), body)), TransitionStep.create(1000F, TweenEquations.QUART_IN).draw((ui, progress, body) -> DemoPushTransition.push(-1920D * progress, body)));
	}

	public static @NonNull DemoPushTransition create() {
		return new DemoPushTransition();
	}

	private static void push(final double x, final Runnable body) {
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(x, 0D, 0D);
			body.run();
		});
	}

}