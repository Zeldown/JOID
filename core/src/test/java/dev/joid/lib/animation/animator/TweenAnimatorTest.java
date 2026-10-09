package dev.joid.lib.animation.animator;

import java.util.ArrayList;
import java.util.Collections;
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
		Assert.assertEquals(0F, animator.getValue(), 0F);
		Assert.assertEquals(1F, animator.getSpeed(), 0F);
		Assert.assertEquals(0L, animator.getLastUpdate());
		Assert.assertNull(animator.getTimeline());
		Assert.assertNotNull(animator.getManager());
	}

	@Test
	public void startsFromItsValue() {
		Assert.assertEquals(5F, TweenAnimator.create(5F).getValue(), 0F);
	}

	@Test
	public void playsASequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).push(1F, 30F).start();
		animator.update(0.5F);
		Assert.assertEquals(5F, animator.getValue(), 0F);
		animator.update(1F);
		Assert.assertEquals(20F, animator.getValue(), 0F);
		animator.update(1F);
		Assert.assertEquals(30F, animator.getValue(), 0F);
	}

	@Test
	public void easesASequenceWithItsEquations() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F, TweenEquations.QUAD_IN).push(1F, 30F, TweenEquations.QUAD_OUT).start();
		animator.update(0.5F);
		Assert.assertEquals(2.5F, animator.getValue(), 0F);
		animator.update(1F);
		Assert.assertEquals(25F, animator.getValue(), 0F);
	}

	@Test
	public void playsInParallel() {
		final TweenAnimator animator = TweenAnimator.create().parallel(2F, 10F).start();
		animator.update(1F);
		Assert.assertEquals(5F, animator.getValue(), 0F);
	}

	@Test
	public void easesInParallelWithItsEquation() {
		final TweenAnimator animator = TweenAnimator.create().parallel(1F, 10F, TweenEquations.QUAD_IN).start();
		animator.update(0.5F);
		Assert.assertEquals(2.5F, animator.getValue(), 0F);
	}

	@Test
	public void scalesTheElapsedTimeBySpeed() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		animator.setSpeed(2F);
		animator.update(0.25F);
		Assert.assertEquals(5F, animator.getValue(), 0F);
	}

	@Test
	public void readsTheElapsedTimeFromTheClock() {
		final TweenAnimator animator = TweenAnimator.create().sequence(100F, 10F).start();
		Assert.assertEquals(1000L, animator.getLastUpdate());
		this.clock.advance(25L);
		animator.update();
		Assert.assertEquals(2.5F, animator.getValue(), 0F);
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
		Assert.assertEquals(10F, animator.getValue(), 0F);
	}

	@Test
	public void killsThePreviousTimelineOfANewSequence() {
		final TweenAnimator animator = TweenAnimator.create().sequence(1F, 10F).start();
		final Timeline previous = animator.getTimeline();
		animator.sequence(1F, 20F).start();
		Assert.assertTrue(previous.isFinished());
		animator.update(0.5F);
		Assert.assertEquals(1, animator.getManager().size());
		Assert.assertEquals(10F, animator.getValue(), 0F);
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
	public void clearsItsAnimation() {
		final TweenAnimator animator = TweenAnimator.create(5F).sequence(1F, 10F).start();
		animator.setSpeed(2F);
		animator.clear();
		Assert.assertEquals(0F, animator.getValue(), 0F);
		Assert.assertEquals(1F, animator.getSpeed(), 0F);
		Assert.assertEquals(0L, animator.getLastUpdate());
		Assert.assertNull(animator.getTimeline());
		animator.update(1F);
		Assert.assertEquals(0F, animator.getValue(), 0F);
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
		Assert.assertEquals(3F, animator.getValue(), 0F);
		Assert.assertEquals(42L, animator.getLastUpdate());
		Assert.assertSame(timeline, animator.getTimeline());
		Assert.assertSame(manager, animator.getManager());
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

}