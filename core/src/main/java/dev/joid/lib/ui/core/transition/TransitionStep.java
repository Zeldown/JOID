package dev.joid.lib.ui.core.transition;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

public class TransitionStep {

	private final float         duration;
	private final TweenEquation equation;

	@Getter
	private final TweenAnimator animator;

	private boolean            drawn;
	private ITransitionDrawing draw;

	@Getter private boolean running;
	@Getter private boolean enabled;

	protected TransitionStep(final float duration, final @NonNull TweenEquation equation) {
		this.duration = duration;
		this.equation = equation;
		this.animator = TweenAnimator.create(0F);
		this.draw = (ui, progress, body) -> body.run();
		this.enabled = true;
	}

	public static @NonNull TransitionStep create(final float duration, final @NonNull TweenEquation equation) {
		return new TransitionStep(duration, equation);
	}

	public final <T extends TransitionStep> @NonNull T draw(final @NonNull ITransitionDrawing draw) {
		this.draw = draw;
		return (T) this;
	}

	public void init(final @NonNull UI ui) {}

	public final void start() {
		this.animator.value(0F);
		if (!this.enabled) {
			return;
		}

		this.drawn = false;
		this.running = true;
		this.animator.sequence(this.duration, 1F, this.equation);
	}

	public final void stop() {
		if (this.animator.getTimeline() != null && !this.animator.getTimeline().isFinished()) {
			this.animator.value(0F);
		}
		this.running = false;
	}

	public final void render(final @NonNull UI ui, final @NonNull Runnable body) {
		if (!this.running) {
			body.run();
			return;
		}

		this.update();
		try {
			this.draw.draw(ui, this.animator.peek(), body);
		} finally {
			this.endFrame();
		}
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	private void update() {
		if (!this.drawn) {
			return;
		}

		if (!this.animator.isAutomatic()) {
			this.animator.update();
		}

		if (this.animator.getTimeline() == null) {
			this.running = false;
		}
	}

	private void endFrame() {
		if (!this.running || this.drawn) {
			return;
		}

		this.drawn = true;
		this.animator.start();
	}

}