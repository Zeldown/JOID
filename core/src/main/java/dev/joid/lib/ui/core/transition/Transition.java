package dev.joid.lib.ui.core.transition;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tweenengine.Timeline;
import dev.joid.lib.ui.core.UI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public abstract class Transition {

	private final In in;
	private final Out out;

	@Getter
	public static abstract class TransitionState {

		private final TweenAnimator animator;
		private boolean running;
		private boolean enabled;

		public TransitionState(final float defaultValue) {
			this.animator = TweenAnimator.create(defaultValue);
			this.running = false;
			this.enabled = true;
		}

		public void start(final @NonNull Timeline timeline) {
			if (!this.enabled) {
				return;
			}

			this.running = true;
			this.animator.setTimeline(timeline);
			this.animator.start();
		}

		public abstract void start();
		public abstract void init(final @NonNull UI ui);
		public abstract void pre(final @NonNull UI ui, final double mouseX, final double mouseY);
		public abstract void post(final @NonNull UI ui, final double mouseX, final double mouseY);

		public final void update() {
			if (!this.running) {
				return;
			}

			this.animator.update();
		}

		public void enable() {
			this.enabled = true;
		}

		public void disable() {
			this.enabled = false;
		}

	}

	public static abstract class In extends TransitionState {

		public In() {
			super(0F);
		}

	}

	public static abstract class Out extends TransitionState {

		public Out() {
			super(1F);
		}

	}

}