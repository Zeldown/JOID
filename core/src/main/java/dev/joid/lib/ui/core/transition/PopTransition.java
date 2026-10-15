package dev.joid.lib.ui.core.transition;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.view.UIView;
import lombok.NonNull;

public class PopTransition extends Transition {

	protected PopTransition() {
		super(TransitionStep.create(130F, TweenEquations.QUART_OUT).draw(PopTransition::pop), TransitionStep.create(130F, TweenEquations.QUART_IN).draw((ui, progress, body) -> PopTransition.pop(ui, 1F - progress, body)));
	}

	public static @NonNull PopTransition create() {
		return new PopTransition();
	}

	private static void pop(final UI ui, final float shown, final Runnable body) {
		final double scale = 0.75D + shown * 0.25D;
		final UIView view = ui.getView();
		final double pivotX = view.getOffsetX() + view.getAnchorX();
		final double pivotY = view.getOffsetY() + view.getAnchorY();
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(pivotX, pivotY, 0D);
			matrix.scale(scale, scale, 1D);
			matrix.translate(-pivotX, -pivotY, 0D);
			body.run();
		});
	}

}