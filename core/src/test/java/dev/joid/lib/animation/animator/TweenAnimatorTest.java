package dev.joid.lib.animation.animator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
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
import dev.joid.lib.animation.tween.Tween;
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
		Assert.assertEquals(0L, TweenAnimatorTest.lastUpdate(animator));
		Assert.assertNull(animator.getTimeline());
		Assert.assertNotNull(TweenAnimatorTest.manager(animator));
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
		animator.onChange(values::add);
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
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).speed(2F).start();
		animator.update(0.25F);
		Assert.assertEquals(5F, animator.get(), 0F);
	}

	@Test
	public void readsTheElapsedTimeFromTheClock() {
		final TweenAnimator animator = TweenAnimator.create().sequence(100F, 10F).start();
		Assert.assertEquals(1000L, TweenAnimatorTest.lastUpdate(animator));
		this.clock.advance(25L);
		animator.update();
		Assert.assertEquals(2.5F, animator.get(), 0F);
		Assert.assertEquals(1025L, TweenAnimatorTest.lastUpdate(animator));
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
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).repeatYoyo(1, 0F);
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
		Assert.assertEquals(1, TweenAnimatorTest.manager(animator).size());
		Assert.assertEquals(10F, animator.get(), 0F);
	}

	@Test
	public void clearsItsAnimation() {
		final TweenAnimator animator = TweenAnimator.create(5F).sequence(1F, 10F).speed(2F).start();
		animator.clear();
		Assert.assertEquals(0F, animator.get(), 0F);
		Assert.assertEquals(1F, animator.getSpeed(), 0F);
		Assert.assertEquals(0L, TweenAnimatorTest.lastUpdate(animator));
		Assert.assertNull(animator.getTimeline());
		animator.update(1F);
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void killsItsTimelineWhenItClears() {
		final List<BaseTween<?>> ends = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).onComplete(event -> ends.add(event.getTween())).start();
		final TweenManager manager = TweenAnimatorTest.manager(animator);
		animator.clear();
		Assert.assertEquals(0, manager.size());
		animator.update(2F);
		Assert.assertTrue(ends.isEmpty());
	}

	@Test
	public void takesATimelineInPlaceOfTheRunningOne() {
		final TweenAnimator animator = TweenAnimator.create().sequence(10F, 1F).start();
		final Timeline running = animator.getTimeline();
		final Timeline timeline = Timeline.createSequence().push(animator.tween(10F).target(4F));
		animator.timeline(timeline).start();
		Assert.assertSame(timeline, animator.getTimeline());
		Assert.assertNotSame(running, animator.getTimeline());
		animator.update(10F);
		Assert.assertEquals(4F, animator.get(), 0F);
	}

	@Test
	public void keepsItsTimelineWhenGivenAgain() {
		final TweenAnimator animator = TweenAnimator.create().sequence(10F, 1F);
		animator.timeline(animator.getTimeline()).start();
		animator.update(10F);
		Assert.assertEquals(1F, animator.get(), 0F);
	}

	@Test
	public void restartsATimelineItKilled() {
		final TweenAnimator animator = TweenAnimator.create();
		final Timeline in = Timeline.createSequence().push(animator.tween(10F).target(1F));
		final Timeline out = Timeline.createSequence().push(animator.tween(10F).target(0F));
		animator.timeline(in).start();
		animator.update(5F);
		animator.timeline(out).start();
		animator.timeline(in).start();
		animator.update(10F);
		Assert.assertEquals(1F, animator.get(), 0F);
	}

	@Test
	public void chainsASequenceFromItsCompletion() {
		final TweenAnimator animator = TweenAnimator.create();
		animator.sequence(1F, 10F).onComplete(event -> animator.sequence(1F, 0F).start()).start();
		animator.update(1F);
		animator.update(1F);
		animator.update(1F);
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void repeatsItsTimeline() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).repeat(1, 0F).start();
		animator.update(1F);
		animator.update(0.25F);
		Assert.assertEquals(2.5F, animator.get(), 0F);
	}

	@Test
	public void repeatsItsTimelineBackAndForth() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).repeatYoyo(1, 0F).start();
		animator.update(1F);
		animator.update(0.25F);
		Assert.assertEquals(7.5F, animator.get(), 0F);
	}

	@Test
	public void tweensItsOwnValueLinearly() {
		final TweenAnimator animator = TweenAnimator.create(2F);
		final Tween tween = animator.tween(10F).target(6F);
		animator.timeline(Timeline.createSequence().push(tween)).start();
		animator.update(5F);
		Assert.assertEquals(4F, animator.get(), 0F);
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
		animator.value(0.5F);
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.5F, animator.get(), 0F);
	}

	@Test
	public void runsToItsEndWithoutAnyReference() {
		final List<String> calls = new ArrayList<>();
		new AnimatorUI(TweenAnimator.create().sequence(32F, 1F).onComplete(event -> calls.add("complete"))).load(1920D, 1080D);
		System.gc();
		this.clock.advance(48L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(Collections.singletonList("complete"), calls);
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
		failing.onChange(value -> {
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
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] The update of TweenAnimator failed (java.lang.IllegalStateException: boom"));
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
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] The onComplete listener of Timeline failed (java.lang.IllegalStateException: boom"));
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
		animator.onChange(values::add);
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
		animator.onChange(values::add);
		animator.update(1F);
		animator.update(1F);
		Assert.assertTrue(values.isEmpty());
	}

	@Test
	public void staysSilentOnceItsAutomaticAnimationEnds() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		animator.onChange(values::add);
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
		animator.onChange(values::add);
		animator.update(1F);
		final Timeline timeline = animator.getTimeline();
		animator.value(2F);
		Assert.assertTrue(timeline.isFinished());
		Assert.assertNull(animator.getTimeline());
		animator.update(1F);
		animator.value(2F);
		Assert.assertEquals(2F, animator.get(), 0F);
		Assert.assertEquals(Arrays.asList(5F, 2F), values);
	}

	@Test
	public void leavesTheRunningAnimatorsWhenItsValueIsSet() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		animator.value(0.25F);
		Assert.assertFalse(animator.isAutomatic());
		this.clock.advance(16L);
		BridgeHandler.UI.fireFrame();
		Assert.assertEquals(0.25F, animator.get(), 0F);
	}

	@Test
	public void refusesToBeWrittenLikeAPlainSignal() {
		final TweenAnimator animator = TweenAnimator.create(0.5F);
		Assert.assertFalse(animator.isWritable());
		try {
			animator.set(1F);
			Assert.fail("An animator cannot be set");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The TweenAnimator{0.5} is animated and cannot be set to 1.0, use value(...) or start a tween", expected.getMessage());
		}
		try {
			animator.reset();
			Assert.fail("An animator cannot be reset");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The TweenAnimator{0.5} is animated and cannot be reset, use value(...) or start a tween", expected.getMessage());
		}
		Assert.assertSame(animator, animator.value(0.5F).clear());
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void staysDistinctFromAnAnimatorOfTheSameValue() {
		Assert.assertNotEquals(TweenAnimator.create(), TweenAnimator.create());
		Assert.assertEquals(2, new HashSet<>(Arrays.asList(TweenAnimator.create(), TweenAnimator.create())).size());
	}

	private static long lastUpdate(final TweenAnimator animator) {
		return (long) TweenAnimatorTest.field(animator, "lastUpdate");
	}

	private static TweenManager manager(final TweenAnimator animator) {
		return (TweenManager) TweenAnimatorTest.field(animator, "manager");
	}

	private static Object field(final TweenAnimator animator, final String name) {
		try {
			final Field field = TweenAnimator.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(animator);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
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