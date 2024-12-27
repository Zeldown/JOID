package be.zeldown.joid.lib.ui.core.transition.impl;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.animation.tweenengine.Timeline;
import be.zeldown.joid.lib.animation.tweenengine.TweenEquations;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.core.transition.Transition;
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

			GL11.glPushMatrix();
			GL11.glTranslated(ui.getData().getAnchorPositionX(), ui.getData().getAnchorPositionY(), 0);
			GL11.glScaled(scale, scale, 1D);
			GL11.glTranslated(-ui.getData().getAnchorPositionX(), -ui.getData().getAnchorPositionY(), 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPopMatrix();
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

			GL11.glPushMatrix();
			GL11.glTranslated(ui.getData().getAnchorPositionX(), ui.getData().getAnchorPositionY(), 0);
			GL11.glScaled(scale, scale, 1D);
			GL11.glTranslated(-ui.getData().getAnchorPositionX(), -ui.getData().getAnchorPositionY(), 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPopMatrix();
		}

	}

}