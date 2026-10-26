package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.TestUtils;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;

public class BaseTweenTest {

	@Test
	public void staysIdleUntilStarted() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value);
		tween.update(0.5F);
		Assert.assertFalse(tween.isStarted());
		Assert.assertFalse(tween.isInitialized());
		Assert.assertEquals(0F, value.getValue(), 0F);
	}

	@Test
	public void reachesItsTargetAndFinishes() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		Assert.assertTrue(tween.isStarted());
		tween.update(0.25F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
		Assert.assertEquals(0.25F, tween.getCurrentTime(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(1F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void startsThroughAManager() {
		final TweenManager manager = new TweenManager();
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F)).start(manager);
		Assert.assertTrue(tween.isStarted());
		Assert.assertEquals(1, manager.size());
	}

	@Test
	public void waitsForItsDelay() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).delay(0.5F).start();
		tween.update(0.25F);
		Assert.assertFalse(tween.isInitialized());
		Assert.assertEquals(0F, value.getValue(), 0F);
		tween.update(0.5F);
		Assert.assertTrue(tween.isInitialized());
		Assert.assertEquals(2.5F, value.getValue(), 0F);
	}

	@Test
	public void addsUpItsDelays() {
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F)).delay(0.5F).delay(0.25F);
		Assert.assertEquals(0.75F, tween.getDelay(), 0F);
		Assert.assertEquals(1.75F, tween.getFullDuration(), 0F);
	}

	@Test
	public void freezesWhilePaused() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.pause();
		Assert.assertTrue(tween.isPaused());
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
		tween.resume();
		Assert.assertFalse(tween.isPaused());
		tween.update(0.25F);
		Assert.assertEquals(5F, value.getValue(), 0F);
	}

	@Test
	public void stopsOnceKilled() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.kill();
		Assert.assertTrue(tween.isFinished());
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
	}

	@Test
	public void repeatsAfterItsRepeatDelay() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0.5F).start();
		Assert.assertEquals(1, tween.getRepeatCount());
		Assert.assertEquals(0.5F, tween.getRepeatDelay(), 0F);
		Assert.assertEquals(2.5F, tween.getFullDuration(), 0F);
		Assert.assertFalse(tween.isYoyo());
		tween.update(1.25F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(1F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void playsBackwardOnEveryOtherYoyoIteration() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		Assert.assertTrue(tween.isYoyo());
		tween.update(1F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		tween.update(0.25F);
		Assert.assertEquals(7.5F, value.getValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void repeatsForeverWithAnInfiniteCount() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(BaseTween.INFINITY, 0F).start();
		Assert.assertEquals(-1F, tween.getFullDuration(), 0F);
		tween.update(1.5F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		for (int i = 0; i < 10; i++) {
			tween.update(1F);
		}
		Assert.assertEquals(5F, value.getValue(), 0F);
		Assert.assertFalse(tween.isFinished());
	}

	@Test
	public void refusesToRepeatForeverWithoutDurationNorRepeatDelay() {
		final TweenValue value = new TweenValue(0F);
		final List<BaseTween<?>> tweenList = Arrays.asList(value.to(0F).target(10F).repeat(BaseTween.INFINITY, 0F), Timeline.createSequence().push(value.to(0F).target(10F)).repeatYoyo(BaseTween.INFINITY, 0F));
		for (final BaseTween<?> tween : tweenList) {
			try {
				tween.start();
				Assert.fail();
			} catch (final IllegalStateException expected) {
				Assert.assertEquals("A tween or timeline that repeats forever needs a duration or a repeat delay above 0, give it one or a finite repeat count", expected.getMessage());
			}
		}
		value.to(0F).target(10F).repeat(BaseTween.INFINITY, 1F).start().update(2.5F);
		Assert.assertEquals(10F, value.getValue(), 0F);
	}

	@Test
	public void ignoresANegativeRepeatDelay() {
		Assert.assertEquals(0F, BaseTweenTest.tween(new TweenValue(0F)).repeat(2, -1F).getRepeatDelay(), 0F);
		Assert.assertEquals(0F, BaseTweenTest.tween(new TweenValue(0F)).repeatYoyo(2, -1F).getRepeatDelay(), 0F);
	}

	@Test
	public void dropsTheYoyoOnAPlainRepeat() {
		Assert.assertFalse(BaseTweenTest.tween(new TweenValue(0F)).repeatYoyo(1, 0F).repeat(1, 0F).isYoyo());
	}

	@Test
	public void refusesToRepeatOnceStarted() {
		try {
			BaseTweenTest.tween(new TweenValue(0F)).start().repeat(1, 0F);
			Assert.fail("A started tween must keep its repetitions");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The repetitions of a tween or timeline cannot change once it is started, call repeat(...) before start()", expected.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToRepeatYoyoOnceStarted() {
		BaseTweenTest.tween(new TweenValue(0F)).start().repeatYoyo(1, 0F);
	}

	@Test
	public void rewindsWithANegativeUpdate() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(2F);
		tween.update(-1.25F);
		Assert.assertEquals(7.5F, value.getValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(-1F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void restartsWhenPlayedForwardAgain() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(2F);
		tween.update(-3F);
		tween.update(1.5F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		Assert.assertFalse(tween.isFinished());
	}

	@Test
	public void rewindsThroughItsRepeatDelay() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0.5F).start();
		tween.update(2F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		tween.update(-0.75F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		tween.update(-0.5F);
		Assert.assertEquals(7.5F, value.getValue(), 0F);
		Assert.assertEquals(0, tween.getStep());
	}

	@Test
	public void rewindsAYoyo() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		tween.update(1.5F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		tween.update(-1F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		Assert.assertEquals(0, tween.getStep());
	}

	@Test
	public void rewindsIntoABackwardYoyoIteration() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(2, 0.5F).start();
		tween.update(2.75F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertEquals(3, tween.getStep());
		tween.update(-0.5F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
		Assert.assertEquals(2, tween.getStep());
	}

	@Test
	public void rewindsForeverWithAnInfiniteCount() {
		final List<String> events = new ArrayList<>();
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(value).repeat(BaseTween.INFINITY, 0F), events).start();
		tween.update(0.5F);
		events.clear();
		tween.update(-1F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		Assert.assertEquals(Arrays.asList("onReverseIterationEnd", "onReverseIterationStart"), events);
	}

	@Test
	public void notifiesEveryForwardStep() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new TweenValue(0F)), events).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onComplete"), events);
	}

	@Test
	public void notifiesEveryBackwardStep() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new TweenValue(0F)), events).start();
		tween.update(2F);
		events.clear();
		tween.update(-3F);
		Assert.assertEquals(Arrays.asList("onReverseStart", "onReverseIterationStart", "onReverseIterationEnd", "onReverseComplete"), events);
	}

	@Test
	public void notifiesEachIterationOfItsRepetitions() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new TweenValue(0F)).repeat(2, 0.5F), events).start();
		tween.update(5F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onComplete"), events);
	}

	@Test
	public void notifiesEachWayOfAYoyoForward() {
		final List<String> events = new ArrayList<>();
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(value).repeatYoyo(1, 0F), events).start();
		tween.update(1.5F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart"), events);
		Assert.assertEquals(5F, value.getValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onComplete"), events);
		Assert.assertEquals(0F, value.getValue(), 0F);
	}

	@Test
	public void startsOnceAfterItsDelay() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new TweenValue(0F)).delay(0.5F), events).start();
		tween.update(0.25F);
		Assert.assertTrue(events.isEmpty());
		tween.update(0.5F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart"), events);
		tween.update(0.25F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart"), events);
	}

	@Test
	public void notifiesOnlyItsListeners() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F)).onStart(event -> events.add("start")).onIterationEnd(event -> events.add("end")).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("start", "end"), events);
	}

	@Test
	public void runsItsListenersInTheOrderOfTheirPriority() {
		final List<String> calls = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F)).onComplete(event -> calls.add("first")).onComplete(event -> calls.add("second")).onComplete(EventListener.priority(EventPriority.HIGH, event -> calls.add("high"))).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("high", "first", "second"), calls);
	}

	@Test
	public void givesItselfInItsEvents() {
		final List<BaseTween<?>> starts = new ArrayList<>();
		final List<BaseTween<?>> ends = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F)).onIterationStart(event -> starts.add(event.getTween())).onIterationEnd(event -> ends.add(event.getTween())).start();
		tween.update(0.5F);
		Assert.assertEquals(Collections.singletonList(tween), starts);
		Assert.assertTrue(ends.isEmpty());
		tween.update(1F);
		Assert.assertEquals(Collections.singletonList(tween), starts);
		Assert.assertEquals(Collections.singletonList(tween), ends);
	}

	@Test
	public void keepsGoingWhenAListenerFails() {
		final List<String> calls = new ArrayList<>();
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0F).onIterationEnd(event -> {
			throw new IllegalStateException("boom");
		}).onIterationEnd(event -> calls.add("end")).onComplete(event -> calls.add("complete")).start();
		final String output = TestUtils.captureErr(() -> tween.update(3F));
		Assert.assertEquals(Arrays.asList("end", "end", "complete"), calls);
		Assert.assertEquals(10F, value.getValue(), 0F);
		Assert.assertTrue(output, output.startsWith("[JOID] The onIterationEnd listener of Tween failed (java.lang.IllegalStateException: boom"));
		Assert.assertEquals(output, output.indexOf("[JOID]"), output.lastIndexOf("[JOID]"));
	}

	@Test
	public void keepsItsUserData() {
		final Tween tween = BaseTweenTest.tween(new TweenValue(0F));
		Assert.assertNull(tween.getUserData());
		Assert.assertSame(tween, tween.userData("data"));
		Assert.assertEquals("data", tween.getUserData());
	}

	@Test
	public void forcesItsEndValues() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.forceToEnd(1F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		Assert.assertEquals(0F, tween.getCurrentTime(), 0F);
	}

	@Test
	public void forcesTheStartValuesAtTheEndOfAYoyo() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		tween.update(0.25F);
		tween.forceToEnd(2F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertEquals(3, tween.getStep());
	}

	@Test
	public void forcesItsStartValues() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = BaseTweenTest.tween(value).delay(0.25F).start();
		tween.update(0.75F);
		tween.forceToStart();
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertEquals(-1, tween.getStep());
		Assert.assertEquals(-0.25F, tween.getCurrentTime(), 0F);
	}

	@Test
	public void drivesACustomTween() {
		final StepTween tween = new StepTween(1F).repeat(2, 0F).start();
		tween.update(3.5F);
		Assert.assertEquals(2, tween.starts);
		Assert.assertTrue(tween.isFinished());
	}

	private static Tween tween(final TweenValue value) {
		return value.to(1F).target(10F).easing(TweenEquations.LINEAR);
	}

	private static Tween record(final Tween tween, final List<String> events) {
		return tween
		.onStart(event -> events.add("onStart"))
		.onIterationStart(event -> events.add("onIterationStart"))
		.onIterationEnd(event -> events.add("onIterationEnd"))
		.onComplete(event -> events.add("onComplete"))
		.onReverseStart(event -> events.add("onReverseStart"))
		.onReverseIterationStart(event -> events.add("onReverseIterationStart"))
		.onReverseIterationEnd(event -> events.add("onReverseIterationEnd"))
		.onReverseComplete(event -> events.add("onReverseComplete"));
	}

	private static final class StepTween extends BaseTween<StepTween> {

		private int starts;

		private StepTween(final float duration) {
			super.duration = duration;
		}

		@Override
		protected void forceEndValues() {}

		@Override
		protected void forceStartValues() {
			this.starts++;
		}

	}

}