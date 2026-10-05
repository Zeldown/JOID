package dev.joid.demo.ui;

import dev.joid.lib.animation.tweenengine.Timeline;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.render.modifier.Vector;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.transition.Transition;
import lombok.NonNull;

public class DemoPushTransition extends Transition {

	public DemoPushTransition() {
		super(new PushInTransition(), new PushOutTransition());
	}

	public static class PushInTransition extends Transition.In {

		private final Transformation transformation = Transformation.create().translate(Vector.X(() -> 1920D * (1F - super.getAnimator().getValue())));

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(1000F, 1F, TweenEquations.QUART_OUT).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.transformation.apply();
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.transformation.reset();
		}

	}

	public static class PushOutTransition extends Transition.Out {

		private final Transformation transformation = Transformation.create().translate(Vector.X(() -> -1920D * (1F - super.getAnimator().getValue())));

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(1000F, 0F, TweenEquations.QUART_IN).getTimeline();
			this.start(timeline);
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.transformation.apply();
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.transformation.reset();
		}

	}

}