package dev.joid.lib.ui.core.transition.impl;

import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.view.UIView;
import lombok.NonNull;

public class PopTransition extends Transition {

	public PopTransition() {
		super(new PopInTransition(), new PopOutTransition());
	}

	public static class PopInTransition extends Transition.In {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(130F, 1F, TweenEquations.QUART_OUT).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			final double scale = 0.75D + super.getAnimator().getValue() * 0.25D;

			final UIView view = ui.getView();
			final double pivotX = view.getOffsetX() + view.getAnchorX();
			final double pivotY = view.getOffsetY() + view.getAnchorY();

			final IRenderBridge render = BridgeHandler.RENDER.get();
			render.getModelView().push();
			render.getModelView().translate(pivotX, pivotY, 0);
			render.getModelView().scale(scale, scale, 1D);
			render.getModelView().translate(-pivotX, -pivotY, 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			BridgeHandler.RENDER.get().getModelView().pop();
		}

	}

	public static class PopOutTransition extends Transition.Out {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(130F, 0F, TweenEquations.QUART_IN).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			final double scale = 0.75D + super.getAnimator().getValue() * 0.25D;

			final UIView view = ui.getView();
			final double pivotX = view.getOffsetX() + view.getAnchorX();
			final double pivotY = view.getOffsetY() + view.getAnchorY();

			final IRenderBridge render = BridgeHandler.RENDER.get();
			render.getModelView().push();
			render.getModelView().translate(pivotX, pivotY, 0);
			render.getModelView().scale(scale, scale, 1D);
			render.getModelView().translate(-pivotX, -pivotY, 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			BridgeHandler.RENDER.get().getModelView().pop();
		}

	}

}