package dev.joid.demo.ui.core.transition.impl;

import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.transition.Transition;
import lombok.NonNull;

public class DemoSlideTransition extends Transition {

	public DemoSlideTransition() {
		super(new SlideInTransition(), new SlideOutTransition());
	}

	public static class SlideInTransition extends Transition.In {

		private final Transformation transformation = Transformation.create().translate(Vector.Y(() -> 1080D * (1F - super.getAnimator().getValue())));

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(500F, 1F, TweenEquations.BACK_OUT).getTimeline();
			super.start(timeline);
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

	public static class SlideOutTransition extends Transition.Out {

		private final Transformation transformation = Transformation.create().translate(Vector.Y(() -> 1080D * (1F - super.getAnimator().getValue())));

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(400F, 0F, TweenEquations.QUART_IN).getTimeline();
			super.start(timeline);
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