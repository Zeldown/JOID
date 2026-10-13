package dev.joid.lib.animation.animator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.bridge.clock.SystemClockBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;

public class TweenAnimatorTest {

	private ManualClockBridge clock;

	@Before
	public void useAManualClock() {
		this.clock = ManualClockBridge.create(1000L);
		BridgeHandler.CLOCK.register(this.clock);
	}

	@After
	public void restoreTheSystemClock() {
		BridgeHandler.CLOCK.register(new SystemClockBridge());
	}

	@Test
	public void startsIdleAtZero() {
		final TweenAnimator animator = TweenAnimator.create();
		Assert.assertEquals(0F, animator.get(), 0F);
		Assert.assertEquals(1F, animator.getSpeed(), 0F);
		Assert.assertEquals(0L, animator.getLastUpdate());
		Assert.assertNull(animator.getTimeline());
		Assert.assertNotNull(animator.getManager());
	}

	@Test
	public void startsFromItsValue() {
		Assert.assertEquals(5F, TweenAnimator.create(5F).get(), 0F);
	}

	@Test
	public void playsASequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).push(1F, 30F).start();
		animator.update(0.5F);
		Assert.assertEquals(5F, animator.get(), 0F);
		animator.update(1F);
		Assert.assertEquals(20F, animator.get(), 0F);
		animator.update(1F);
		Assert.assertEquals(30F, animator.get(), 0F);
	}

	@Test
	public void easesASequenceWithItsEquations() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F, TweenEquations.QUAD_IN).push(1F, 30F, TweenEquations.QUAD_OUT).start();
		animator.update(0.5F);
		Assert.assertEquals(2.5F, animator.get(), 0F);
		animator.update(1F);
		Assert.assertEquals(25F, animator.get(), 0F);
	}

	@Test
	public void chainsItsStepsOnItsSingleValue() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create(4F).sequence(1F, 10F).push(1F, 30F).push(1F, 30F).start();
		animator.subscribe(values::add);
		animator.update(1.5F);
		Assert.assertEquals(20F, animator.get(), 0F);
		animator.update(1F);
		animator.update(1F);
		Assert.assertEquals(30F, animator.get(), 0F);
		Assert.assertEquals(Arrays.asList(20F, 30F), values);
	}

	@Test
	public void animatesSeveralValuesWithSeveralAnimatorsStartedTogether() {
		final TweenAnimator horizontal = TweenAnimator.create().sequence(32F, 1F);
		final TweenAnimator vertical = TweenAnimator.create().sequence(64F, 1F);
		new AnimatorUI(horizontal, vertical).load(1920D, 1080D);
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.5F, horizontal.get(), 0F);
		Assert.assertEquals(0.25F, vertical.get(), 0F);
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(1F, horizontal.get(), 0F);
		Assert.assertEquals(0.5F, vertical.get(), 0F);
	}

	@Test
	public void scalesTheElapsedTimeBySpeed() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		animator.setSpeed(2F);
		animator.update(0.25F);
		Assert.assertEquals(5F, animator.get(), 0F);
	}

	@Test
	public void readsTheElapsedTimeFromTheClock() {
		final TweenAnimator animator = TweenAnimator.create().sequence(100F, 10F).start();
		Assert.assertEquals(1000L, animator.getLastUpdate());
		this.clock.advance(25L);
		animator.update();
		Assert.assertEquals(2.5F, animator.get(), 0F);
		Assert.assertEquals(1025L, animator.getLastUpdate());
	}

	@Test
	public void notifiesTheEndOfItsTimeline() {
		final List<BaseTween<?>> ends = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).push(1F, 20F).onComplete(event -> ends.add(event.getTween())).start();
		final Timeline timeline = animator.getTimeline();
		animator.update(1.5F);
		Assert.assertTrue(ends.isEmpty());
		animator.update(1F);
		Assert.assertEquals(Collections.singletonList(timeline), ends);
		animator.update(1F);
		Assert.assertEquals(Collections.singletonList(timeline), ends);
	}

	@Test
	public void notifiesEveryStepOfItsTimeline() {
		final List<String> calls = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F);
		animator.getTimeline().repeatYoyo(1, 0F);
		animator
		.onStart(event -> calls.add("start"))
		.onIterationStart(event -> calls.add("iterationStart"))
		.onIterationEnd(event -> calls.add("iterationEnd"))
		.onComplete(event -> calls.add("complete"))
		.onReverseStart(event -> calls.add("reverseStart"))
		.onReverseIterationStart(event -> calls.add("reverseIterationStart"))
		.onReverseIterationEnd(event -> calls.add("reverseIterationEnd"))
		.onReverseComplete(event -> calls.add("reverseComplete"))
		.start();
		animator.update(0.5F);
		Assert.assertEquals(Arrays.asList("start", "iterationStart"), calls);
		animator.update(1F);
		Assert.assertEquals(Arrays.asList("start", "iterationStart", "iterationEnd", "iterationStart"), calls);
		animator.update(1F);
		Assert.assertEquals(Arrays.asList("start", "iterationStart", "iterationEnd", "iterationStart", "iterationEnd", "complete"), calls);
	}

	@Test
	public void keepsItsTimelineWhileItRuns() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		final Timeline timeline = animator.getTimeline();
		animator.update(0.5F);
		Assert.assertSame(timeline, animator.getTimeline());
	}

	@Test
	public void forgetsItsTimelineOnceItEnds() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		animator.update(2F);
		Assert.assertNull(animator.getTimeline());
		Assert.assertEquals(10F, animator.get(), 0F);
	}

	@Test
	public void killsThePreviousTimelineOfANewSequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		final Timeline previous = animator.getTimeline();
		animator.sequence(1F, 20F).start();
		Assert.assertTrue(previous.isFinished());
		animator.update(0.5F);
		Assert.assertEquals(1, animator.getManager().size());
		Assert.assertEquals(10F, animator.get(), 0F);
	}

	@Test
	public void clearsItsAnimation() {
		final TweenAnimator animator = TweenAnimator.create(5F).sequence(1F, 10F).start();
		animator.setSpeed(2F);
		animator.clear();
		Assert.assertEquals(0F, animator.get(), 0F);
		Assert.assertEquals(1F, animator.getSpeed(), 0F);
		Assert.assertEquals(0L, animator.getLastUpdate());
		Assert.assertNull(animator.getTimeline());
		animator.update(1F);
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void killsAndFreesItsTimelineWhenItClears() {
		final List<BaseTween<?>> ends = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).onComplete(event -> ends.add(event.getTween())).start();
		final TweenManager manager = animator.getManager();
		final int pooled = Timeline.getPoolSize();
		animator.clear();
		Assert.assertEquals(0, manager.size());
		Assert.assertEquals(pooled + 1, Timeline.getPoolSize());
		animator.update(2F);
		Assert.assertTrue(ends.isEmpty());
	}

	@Test
	public void exposesItsState() {
		final TweenAnimator animator = TweenAnimator.create();
		final Timeline timeline = Timeline.createSequence();
		final TweenManager manager = new TweenManager();
		animator.setValue(3F);
		animator.setLastUpdate(42L);
		animator.setTimeline(timeline);
		animator.setManager(manager);
		Assert.assertEquals(3F, animator.get(), 0F);
		Assert.assertEquals(42L, animator.getLastUpdate());
		Assert.assertSame(timeline, animator.getTimeline());
		Assert.assertSame(manager, animator.getManager());
	}

	@Test
	public void advancesOnceAFrameWhenStartedInAUi() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		Assert.assertTrue(animator.isAutomatic());
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.5F, animator.get(), 0F);
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(1F, animator.get(), 0F);
	}

	@Test
	public void leavesTheRunningAnimatorsOnceItsTimelineEnds() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		this.clock.advance(48L);
		BridgeHandler.UI.fireFrame();
		Assert.assertNull(animator.getTimeline());
		Assert.assertFalse(animator.isAutomatic());
		animator.setValue(0.5F);
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.5F, animator.get(), 0F);
	}

	@Test
	public void leavesTheRunningAnimatorsWhenItClears() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		animator.clear();
		Assert.assertFalse(animator.isAutomatic());
	}

	@Test
	public void leavesTheRunningAnimatorsForANewSequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		animator.sequence(32F, 2F);
		Assert.assertFalse(animator.isAutomatic());
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void keepsTheFrameGoingWhenAnAnimatorFails() {
		final TweenAnimator failing = TweenAnimator.create().sequence(64F, 1F);
		final TweenAnimator other = TweenAnimator.create().sequence(64F, 1F);
		failing.subscribe(value -> {
			throw new IllegalStateException("boom");
		});
		new AnimatorUI(failing, other).load(1920D, 1080D);
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.clock.advance(16L);
			BridgeHandler.UI.fireFrame();
			this.clock.advance(16L);
			BridgeHandler.UI.fireFrame();
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals(0.5F, other.get(), 0F);
		Assert.assertEquals(0.5F, failing.get(), 0F);
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] The update of TweenAnimator failed: java.lang.IllegalStateException: boom"));
		Assert.assertEquals(output.toString(), output.toString().indexOf("[JOID]"), output.toString().lastIndexOf("[JOID]"));
	}

	@Test
	public void keepsTheFrameGoingWhenAListenerOfItsTimelineFails() {
		final List<String> calls = new ArrayList<>();
		final TweenAnimator failing = TweenAnimator.create().sequence(16F, 1F);
		final TweenAnimator other = TweenAnimator.create().sequence(64F, 1F);
		failing.onComplete(event -> {
			throw new IllegalStateException("boom");
		}).onComplete(event -> calls.add("complete"));
		new AnimatorUI(failing, other).load(1920D, 1080D);
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.clock.advance(32L);
			BridgeHandler.UI.fireFrame();
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals(Collections.singletonList("complete"), calls);
		Assert.assertEquals(1F, failing.get(), 0F);
		Assert.assertEquals(0.5F, other.get(), 0F);
		Assert.assertFalse(failing.isAutomatic());
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] The onComplete listener of Timeline failed: java.lang.IllegalStateException: boom"));
	}

	@Test
	public void waitsForAManualUpdateWhenStartedOutsideAUi() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F).start();
		Assert.assertFalse(animator.isAutomatic());
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0F, animator.get(), 0F);
		animator.update();
		Assert.assertEquals(0.5F, animator.get(), 0F);
	}

	@Test
	public void refusesToStartWithoutTimeline() {
		try {
			TweenAnimator.create().start();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The animator has no timeline, call sequence(...) first", exception.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAStepWithoutTimeline() {
		TweenAnimator.create().push(1F, 10F);
	}

	@Test(expected = IllegalStateException.class)
	public void refusesACallbackWithoutTimeline() {
		TweenAnimator.create().onComplete(event -> {});
	}

	@Test(expected = NullPointerException.class)
	public void refusesASequenceWithoutEquation() {
		TweenAnimator.create().sequence(1F, 10F, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAStepWithoutEquation() {
		TweenAnimator.create().sequence(1F, 10F).push(1F, 20F, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCallback() {
		TweenAnimator.create().sequence(1F, 10F).onComplete(null);
	}

	@Test
	public void notifiesOnlyWhenItsValueChanges() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(2F, 10F).start();
		animator.subscribe(values::add);
		animator.update(1F);
		animator.update(0F);
		animator.update(1F);
		Assert.assertEquals(Arrays.asList(5F, 10F), values);
	}

	@Test
	public void staysSilentOnceItsAnimationEnds() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		animator.update(2F);
		animator.subscribe(values::add);
		animator.update(1F);
		animator.update(1F);
		Assert.assertTrue(values.isEmpty());
	}

	@Test
	public void staysSilentOnceItsAutomaticAnimationEnds() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		animator.subscribe(values::add);
		new AnimatorUI(animator).load(1920D, 1080D);
		for (int i = 0; i < 5; i++) {
			this.clock.advance(16L);
			BridgeHandler.UI.fireFrame();
		}
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void cutsItsAnimationWhenItsValueIsSet() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(2F, 10F).start();
		animator.subscribe(values::add);
		animator.update(1F);
		final Timeline timeline = animator.getTimeline();
		animator.setValue(2F);
		Assert.assertTrue(timeline.isFinished());
		Assert.assertNull(animator.getTimeline());
		animator.update(1F);
		animator.setValue(2F);
		Assert.assertEquals(2F, animator.get(), 0F);
		Assert.assertEquals(Arrays.asList(5F, 2F), values);
	}

	@Test
	public void leavesTheRunningAnimatorsWhenItsValueIsSet() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		animator.setValue(0.25F);
		Assert.assertFalse(animator.isAutomatic());
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.25F, animator.get(), 0F);
	}

	@Test
	public void refusesToBeSetAsASignal() {
		try {
			TweenAnimator.create().set(1F);
			Assert.fail();
		} catch (final UnsupportedOperationException exception) {
			Assert.assertEquals("A TweenAnimator is read-only and takes the value of its animation: call setValue(...) instead", exception.getMessage());
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void refusesToBeReset() {
		TweenAnimator.create().reset();
	}

	@Test
	public void staysDistinctFromAnAnimatorOfTheSameValue() {
		Assert.assertNotEquals(TweenAnimator.create(), TweenAnimator.create());
		Assert.assertEquals(2, new HashSet<>(Arrays.asList(TweenAnimator.create(), TweenAnimator.create())).size());
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class AnimatorUI extends UI {

		private final TweenAnimator[] animators;

		public AnimatorUI(final TweenAnimator... animators) {
			this.animators = animators;
		}

		@Override
		public void init() {
			for (final TweenAnimator animator : this.animators) {
				animator.start();
			}
		}

	}

}