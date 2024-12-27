package be.zeldown.joid.demo.ui;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.animation.tweenengine.Timeline;
import be.zeldown.joid.lib.animation.tweenengine.TweenEquations;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.core.transition.Transition;
import lombok.NonNull;

public class DemoPushTransition extends Transition {

	public DemoPushTransition() {
		super(new PushInTransition(), new PushOutTransition());
	}

	public static class PushInTransition extends Transition.In {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(1000F, 1F, TweenEquations.QUART_OUT).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPushMatrix();
			GL11.glTranslated(1920 * (1F - super.getAnimator().getValue()), 0, 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPopMatrix();
		}

	}

	public static class PushOutTransition extends Transition.Out {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(1000F, 0F, TweenEquations.QUART_IN).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPushMatrix();
			GL11.glTranslated(-1920 * (1F - super.getAnimator().getValue()), 0, 0);
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			GL11.glPopMatrix();
		}

	}

}