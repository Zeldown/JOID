package dev.joid.lib.ui.core.transition;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.ui.core.UI;

import lombok.NonNull;

public class TransitionTest {

	private final ManualClockBridge clock = ManualClockBridge.create(0L);

	@Before
	public void useAManualClock() {
		BridgeHandler.CLOCK.register(this.clock);
	}

	@After
	public void restoreTheClock() {
		BridgeHandler.CLOCK.unregister(this.clock);
	}

	@Test
	public void keepsItsTwoStates() {
		final FadeIn in = new FadeIn();
		final FadeOut out = new FadeOut();
		final FadeTransition transition = new FadeTransition(in, out);
		Assert.assertSame(in, transition.getIn());
		Assert.assertSame(out, transition.getOut());
	}

	@Test
	public void startsAnInStateHiddenAndAnOutStateShown() {
		Assert.assertEquals(0F, new FadeIn().getAnimator().getValue(), 0F);
		Assert.assertEquals(1F, new FadeOut().getAnimator().getValue(), 0F);
	}

	@Test
	public void waitsEnabledBeforeItsStart() {
		final FadeIn in = new FadeIn();
		Assert.assertTrue(in.isEnabled());
		Assert.assertFalse(in.isRunning());
	}

	@Test
	public void playsItsTimelineOnceStarted() {
		final FadeIn in = new FadeIn();
		in.start();
		Assert.assertTrue(in.isRunning());
		this.clock.advance(50L);
		in.update();
		Assert.assertEquals(0.5F, in.getAnimator().getValue(), 0.0001F);
		this.clock.advance(50L);
		in.update();
		Assert.assertEquals(1F, in.getAnimator().getValue(), 0F);
	}

	@Test
	public void ignoresItsUpdatesBeforeItsStart() {
		final FadeOut out = new FadeOut();
		out.getAnimator().sequence(100F, 0F).start();
		this.clock.advance(50L);
		out.update();
		Assert.assertEquals(1F, out.getAnimator().getValue(), 0F);
	}

	@Test
	public void ignoresItsStartOnceDisabled() {
		final FadeOut out = new FadeOut();
		out.disable();
		out.start();
		this.clock.advance(50L);
		out.update();
		Assert.assertFalse(out.isEnabled());
		Assert.assertFalse(out.isRunning());
		Assert.assertEquals(1F, out.getAnimator().getValue(), 0F);
	}

	@Test
	public void startsAgainOnceEnabled() {
		final FadeOut out = new FadeOut();
		out.disable();
		out.enable();
		out.start();
		this.clock.advance(100L);
		out.update();
		Assert.assertTrue(out.isEnabled());
		Assert.assertTrue(out.isRunning());
		Assert.assertEquals(0F, out.getAnimator().getValue(), 0F);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToStartWithoutTimeline() {
		new FadeIn().start(null);
	}

	@Test
	public void stopsRunningOnceItsTweenEnds() {
		final FadeIn in = new FadeIn();
		in.start();
		this.clock.advance(200L);
		in.update();
		Assert.assertEquals(1F, in.getAnimator().getValue(), 0F);
		Assert.assertFalse(in.isRunning());
	}

	public static final class FadeTransition extends Transition {

		public FadeTransition(final In in, final Out out) {
			super(in, out);
		}

	}

	public static final class FadeIn extends Transition.In {

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(100F, 1F).getTimeline();
			super.start(timeline);
		}

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {}

	}

	public static final class FadeOut extends Transition.Out {

		@Override
		public void start() {
			final Timeline timeline = super.getAnimator().sequence(100F, 0F).getTimeline();
			super.start(timeline);
		}

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {}

	}

}