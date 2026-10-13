package dev.joid.lib.animation.animator;

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
import lombok.AllArgsConstructor;

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
	public void playsInParallel() {
		final TweenAnimator animator = TweenAnimator.create().parallel(2F, 10F).start();
		animator.update(1F);
		Assert.assertEquals(5F, animator.get(), 0F);
	}

	@Test
	public void drivesOneValuePerParallelStep() {
		final TweenAnimator animator = TweenAnimator.create().parallel(2F, 10F).push(1F, 30F).push(4F, 8F, TweenEquations.QUAD_IN).start();
		animator.update(1F);
		Assert.assertEquals(5F, animator.get(), 0F);
		Assert.assertEquals(5F, animator.channel(0).get(), 0F);
		Assert.assertEquals(30F, animator.channel(1).get(), 0F);
		Assert.assertEquals(0.5F, animator.channel(2).get(), 0F);
		Assert.assertEquals(0F, animator.channel(3).get(), 0F);
	}

	@Test
	public void drivesOneValueInSequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).push(1F, 30F).start();
		animator.update(1.5F);
		Assert.assertEquals(20F, animator.get(), 0F);
		Assert.assertEquals(0F, animator.channel(1).get(), 0F);
	}

	@Test
	public void easesInParallelWithItsEquation() {
		final TweenAnimator animator = TweenAnimator.create().parallel(1F, 10F, TweenEquations.QUAD_IN).start();
		animator.update(0.5F);
		Assert.assertEquals(2.5F, animator.get(), 0F);
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
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).setCallback(ends::add).start();
		final Timeline timeline = animator.getTimeline();
		animator.update(0.5F);
		Assert.assertTrue(ends.isEmpty());
		animator.update(1F);
		Assert.assertEquals(Collections.singletonList(timeline), ends);
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
	public void killsThePreviousTimelineOfANewParallel() {
		final TweenAnimator animator = TweenAnimator.create().parallel(1F, 10F).start();
		final Timeline previous = animator.getTimeline();
		animator.parallel(1F, 20F).start();
		Assert.assertTrue(previous.isFinished());
		animator.update(0.5F);
		Assert.assertEquals(1, animator.getManager().size());
	}

	@Test
	public void clearsTheValuesOfItsParallelSteps() {
		final TweenAnimator animator = TweenAnimator.create().parallel(1F, 10F).push(1F, 20F).start();
		animator.update(1F);
		animator.clear();
		Assert.assertEquals(0F, animator.channel(1).get(), 0F);
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
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).setCallback(ends::add).start();
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
		TweenAnimator.updateRunning();
		TweenAnimator.updateRunning();
		Assert.assertEquals(0.5F, animator.get(), 0F);
		this.clock.advance(16L);
		TweenAnimator.updateRunning();
		Assert.assertEquals(1F, animator.get(), 0F);
	}

	@Test
	public void leavesTheRunningAnimatorsOnceItsTimelineEnds() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		this.clock.advance(48L);
		TweenAnimator.updateRunning();
		Assert.assertNull(animator.getTimeline());
		Assert.assertFalse(animator.isAutomatic());
		animator.setValue(0.5F);
		this.clock.advance(16L);
		TweenAnimator.updateRunning();
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
		TweenAnimator.updateRunning();
		Assert.assertEquals(0F, animator.get(), 0F);
	}

	@Test
	public void leavesTheRunningAnimatorsForANewParallel() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		new AnimatorUI(animator).load(1920D, 1080D);
		animator.parallel(32F, 2F);
		Assert.assertFalse(animator.isAutomatic());
	}

	@Test
	public void waitsForAManualUpdateWhenStartedOutsideAUi() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F).start();
		Assert.assertFalse(animator.isAutomatic());
		this.clock.advance(16L);
		TweenAnimator.updateRunning();
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
			Assert.assertEquals("The animator has no timeline, call sequence(...) or parallel(...) first", exception.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAStepWithoutTimeline() {
		TweenAnimator.create().push(1F, 10F);
	}

	@Test(expected = IllegalStateException.class)
	public void refusesACallbackWithoutTimeline() {
		TweenAnimator.create().setCallback(tween -> {});
	}

	@Test(expected = NullPointerException.class)
	public void refusesASequenceWithoutEquation() {
		TweenAnimator.create().sequence(1F, 10F, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAParallelWithoutEquation() {
		TweenAnimator.create().parallel(1F, 10F, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAStepWithoutEquation() {
		TweenAnimator.create().sequence(1F, 10F).push(1F, 20F, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCallback() {
		TweenAnimator.create().sequence(1F, 10F).setCallback(null);
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
			TweenAnimator.updateRunning();
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
		TweenAnimator.updateRunning();
		Assert.assertEquals(0.25F, animator.get(), 0F);
	}

	@Test
	public void notifiesEachChannelOnlyWhenItsValueChanges() {
		final List<Float> first = new ArrayList<>();
		final List<Float> second = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().parallel(2F, 10F).push(1F, 30F).start();
		animator.channel(0).subscribe(first::add);
		animator.channel(1).subscribe(second::add);
		animator.update(1F);
		animator.update(1F);
		animator.update(1F);
		Assert.assertEquals(Arrays.asList(5F, 10F), first);
		Assert.assertEquals(Arrays.asList(30F), second);
	}

	@Test
	public void startsAChannelFromItsValue() {
		final TweenAnimator animator = TweenAnimator.create(4F).parallel(2F, 10F).push(1F, 30F).start();
		animator.update(1F);
		Assert.assertEquals(7F, animator.channel(0).get(), 0F);
		Assert.assertEquals(30F, animator.channel(1).get(), 0F);
		Assert.assertEquals(0F, animator.channel(4).get(), 0F);
		Assert.assertSame(animator.channel(1), animator.channel(1));
	}

	@Test
	public void setsTheValueOfAChannel() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().parallel(2F, 10F).push(1F, 30F).start();
		animator.channel(2).subscribe(values::add);
		animator.setValue(2, 7F);
		Assert.assertNull(animator.getTimeline());
		Assert.assertEquals(7F, animator.channel(2).get(), 0F);
		Assert.assertEquals(Arrays.asList(7F), values);
	}

	@Test
	public void clearsItsChannels() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().parallel(1F, 10F).push(1F, 20F).start();
		animator.update(1F);
		animator.channel(1).subscribe(values::add);
		animator.clear();
		Assert.assertEquals(Arrays.asList(0F), values);
	}

	@Test
	public void exposesItsChannelsAndValuesReadOnly() {
		final TweenAnimator animator = TweenAnimator.create(3F);
		animator.getValues()[0] = 9F;
		Assert.assertEquals(3F, animator.get(), 0F);
		try {
			animator.getChannelMap().put(0, animator.channel(0));
			Assert.fail();
		} catch (final UnsupportedOperationException exception) {
			Assert.assertEquals(1, animator.getChannelMap().size());
		}
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
	public void refusesToSetAChannel() {
		try {
			TweenAnimator.create().channel(1).set(1F);
			Assert.fail();
		} catch (final UnsupportedOperationException exception) {
			Assert.assertEquals("A TweenAnimatorChannel is read-only and takes the value of its animator: call setValue(index, value) on the animator instead", exception.getMessage());
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void refusesToResetAChannel() {
		TweenAnimator.create().channel(1).reset();
	}

	@Test
	public void staysDistinctFromAnAnimatorOfTheSameValue() {
		Assert.assertNotEquals(TweenAnimator.create(), TweenAnimator.create());
		Assert.assertEquals(2, new HashSet<>(Arrays.asList(TweenAnimator.create(), TweenAnimator.create())).size());
	}

	@AllArgsConstructor
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class AnimatorUI extends UI {

		private final TweenAnimator animator;

		@Override
		public void init() {
			this.animator.start();
		}

	}

}