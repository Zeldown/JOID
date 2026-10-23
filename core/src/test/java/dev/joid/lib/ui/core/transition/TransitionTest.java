package dev.joid.lib.ui.core.transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;

public class TransitionTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final UI ui = new EmptyUI();

	@Test
	public void keepsItsTwoSteps() {
		final TransitionStep in = TransitionTest.step();
		final TransitionStep out = TransitionTest.step();
		final FadeTransition transition = new FadeTransition(in, out);
		Assert.assertSame(in, transition.getIn());
		Assert.assertSame(out, transition.getOut());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingStep() {
		new FadeTransition(TransitionTest.step(), null);
	}

	@Test
	public void startsFromNoProgress() {
		Assert.assertEquals(0F, TransitionTest.step().getAnimator().get(), 0F);
	}

	@Test
	public void waitsEnabledBeforeItsStart() {
		final TransitionStep step = TransitionTest.step();
		Assert.assertTrue(step.isEnabled());
		Assert.assertFalse(step.isRunning());
	}

	@Test
	public void playsItsTimelineOnceStarted() {
		final TransitionStep step = TransitionTest.step();
		step.start();
		Assert.assertTrue(step.isRunning());
		this.frame(step);
		this.bridges.getClock().advance(50L);
		this.frame(step);
		Assert.assertEquals(0.5F, step.getAnimator().get(), 0.0001F);
		this.bridges.getClock().advance(50L);
		this.frame(step);
		Assert.assertEquals(1F, step.getAnimator().get(), 0F);
	}

	@Test
	public void waitsItsFirstDrawnFrameBeforeItsClockRuns() {
		final TransitionStep step = TransitionTest.step();
		step.start();
		this.bridges.getClock().advance(500L);
		Assert.assertEquals(0F, step.getAnimator().get(), 0F);
		Assert.assertTrue(step.isRunning());
		this.frame(step);
		this.bridges.getClock().advance(50L);
		this.frame(step);
		Assert.assertEquals(0.5F, step.getAnimator().get(), 0.0001F);
	}

	@Test
	public void countsTheDrawOfTheFramesAfterItsFirst() {
		final TransitionStep step = TransitionTest.step();
		step.start();
		this.frame(step);
		this.bridges.getClock().advance(30L);
		this.frame(step);
		this.bridges.getClock().advance(20L);
		this.frame(step);
		Assert.assertEquals(0.5F, step.getAnimator().get(), 0.0001F);
	}

	@Test
	public void ignoresItsFramesBeforeItsStart() {
		final TransitionStep step = TransitionTest.step();
		step.getAnimator().sequence(100F, 1F).start();
		this.bridges.getClock().advance(50L);
		step.render(this.ui, () -> {});
		Assert.assertEquals(0F, step.getAnimator().get(), 0F);
	}

	@Test
	public void ignoresItsStartOnceDisabled() {
		final TransitionStep step = TransitionTest.step();
		step.enabled(false);
		step.start();
		this.bridges.getClock().advance(50L);
		this.frame(step);
		Assert.assertFalse(step.isEnabled());
		Assert.assertFalse(step.isRunning());
		Assert.assertEquals(0F, step.getAnimator().get(), 0F);
	}

	@Test
	public void startsAgainOnceEnabled() {
		final TransitionStep step = TransitionTest.step();
		step.enabled(false);
		step.enabled(true);
		step.start();
		this.frame(step);
		this.bridges.getClock().advance(100L);
		this.frame(step);
		Assert.assertTrue(step.isEnabled());
		Assert.assertEquals(1F, step.getAnimator().get(), 0F);
	}

	@Test
	public void stopsRunningOnceItsTweenEnds() {
		final TransitionStep step = TransitionTest.step();
		step.start();
		this.frame(step);
		this.bridges.getClock().advance(200L);
		this.frame(step);
		Assert.assertEquals(1F, step.getAnimator().get(), 0F);
		Assert.assertFalse(step.isRunning());
	}

	@Test
	public void replaysFromItsStartWhenStartedAgain() {
		final TransitionStep step = TransitionTest.step();
		step.start();
		this.frame(step);
		this.bridges.getClock().advance(200L);
		this.frame(step);
		Assert.assertEquals(1F, step.getAnimator().get(), 0F);
		step.start();
		Assert.assertEquals(0F, step.getAnimator().get(), 0F);
		this.frame(step);
		this.bridges.getClock().advance(50L);
		this.frame(step);
		Assert.assertEquals(0.5F, step.getAnimator().get(), 0.0001F);
	}

	@Test
	public void drawsTheBodyOfARunningStepThroughItsDrawing() {
		final List<String> trace = new ArrayList<>();
		final TransitionStep step = TransitionTest.step().draw((ui, progress, body) -> {
			trace.add("before " + progress);
			body.run();
			trace.add("after");
		});
		step.render(this.ui, () -> trace.add("body"));
		Assert.assertEquals(Collections.singletonList("body"), trace);
		trace.clear();
		step.start();
		step.render(this.ui, () -> trace.add("body"));
		Assert.assertEquals(Arrays.asList("before 0.0", "body", "after"), trace);
	}

	@Test
	public void drawsItsBodyAsItIsByDefault() {
		final List<String> trace = new ArrayList<>();
		final TransitionStep step = TransitionTest.step();
		step.start();
		step.render(this.ui, () -> trace.add("body"));
		Assert.assertEquals(Collections.singletonList("body"), trace);
	}

	private void frame(final TransitionStep step) {
		BridgeHandler.UI.fireFrame();
		step.render(this.ui, () -> {});
	}

	private static TransitionStep step() {
		return TransitionStep.create(100F, TweenEquations.LINEAR);
	}

	public static final class FadeTransition extends Transition {

		public FadeTransition(final TransitionStep in, final TransitionStep out) {
			super(in, out);
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class EmptyUI extends UI {}

}