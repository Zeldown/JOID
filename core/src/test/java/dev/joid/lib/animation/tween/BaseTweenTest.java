package dev.joid.lib.animation.tween;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tween.primitive.MutableFloat;
import dev.joid.lib.ui.event.EventPriority;

public class BaseTweenTest {

	@Test
	public void staysIdleUntilStarted() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value);
		tween.update(0.5F);
		Assert.assertFalse(tween.isStarted());
		Assert.assertFalse(tween.isInitialized());
		Assert.assertEquals(0F, value.floatValue(), 0F);
	}

	@Test
	public void reachesItsTargetAndFinishes() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		Assert.assertTrue(tween.isStarted());
		tween.update(0.25F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		Assert.assertEquals(0.25F, tween.getCurrentTime(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void startsThroughAManager() {
		final TweenManager manager = new TweenManager();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).start(manager);
		Assert.assertTrue(tween.isStarted());
		Assert.assertEquals(Collections.singletonList(tween), manager.getObjects());
	}

	@Test
	public void waitsForItsDelay() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).delay(0.5F).start();
		tween.update(0.25F);
		Assert.assertFalse(tween.isInitialized());
		Assert.assertEquals(0F, value.floatValue(), 0F);
		tween.update(0.5F);
		Assert.assertTrue(tween.isInitialized());
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
	}

	@Test
	public void addsUpItsDelays() {
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).delay(0.5F).delay(0.25F);
		Assert.assertEquals(0.75F, tween.getDelay(), 0F);
		Assert.assertEquals(1.75F, tween.getFullDuration(), 0F);
	}

	@Test
	public void freezesWhilePaused() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.pause();
		Assert.assertTrue(tween.isPaused());
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		tween.resume();
		Assert.assertFalse(tween.isPaused());
		tween.update(0.25F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
	}

	@Test
	public void stopsOnceKilled() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.kill();
		Assert.assertTrue(tween.isFinished());
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
	}

	@Test
	public void repeatsAfterItsRepeatDelay() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0.5F).start();
		Assert.assertEquals(1, tween.getRepeatCount());
		Assert.assertEquals(0.5F, tween.getRepeatDelay(), 0F);
		Assert.assertEquals(2.5F, tween.getFullDuration(), 0F);
		Assert.assertFalse(tween.isYoyo());
		tween.update(1.25F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		tween.update(0.5F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void playsBackwardOnEveryOtherYoyoIteration() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		Assert.assertTrue(tween.isYoyo());
		tween.update(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		tween.update(0.25F);
		Assert.assertEquals(7.5F, value.floatValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void repeatsForeverWithAnInfiniteCount() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(Tween.INFINITY, 0F).start();
		Assert.assertEquals(-1F, tween.getFullDuration(), 0F);
		tween.update(1.5F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		for (int i = 0; i < 10; i++) {
			tween.update(1F);
		}
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
	}

	@Test
	public void ignoresANegativeRepeatDelay() {
		Assert.assertEquals(0F, BaseTweenTest.tween(new MutableFloat(0F)).repeat(2, -1F).getRepeatDelay(), 0F);
		Assert.assertEquals(0F, BaseTweenTest.tween(new MutableFloat(0F)).repeatYoyo(2, -1F).getRepeatDelay(), 0F);
	}

	@Test
	public void dropsTheYoyoOnAPlainRepeat() {
		Assert.assertFalse(BaseTweenTest.tween(new MutableFloat(0F)).repeatYoyo(1, 0F).repeat(1, 0F).isYoyo());
	}

	@Test(expected = RuntimeException.class)
	public void refusesToRepeatOnceStarted() {
		BaseTweenTest.tween(new MutableFloat(0F)).start().repeat(1, 0F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesToRepeatYoyoOnceStarted() {
		BaseTweenTest.tween(new MutableFloat(0F)).start().repeatYoyo(1, 0F);
	}

	@Test
	public void rewindsWithANegativeUpdate() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(2F);
		tween.update(-1.25F);
		Assert.assertEquals(7.5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		tween.update(-1F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void restartsWhenPlayedForwardAgain() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(2F);
		tween.update(-3F);
		tween.update(1.5F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
	}

	@Test
	public void rewindsThroughItsRepeatDelay() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0.5F).start();
		tween.update(2F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		tween.update(-0.75F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		tween.update(-0.5F);
		Assert.assertEquals(7.5F, value.floatValue(), 0F);
		Assert.assertEquals(0, tween.getStep());
	}

	@Test
	public void rewindsAYoyo() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		tween.update(1.5F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		tween.update(-1F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertEquals(0, tween.getStep());
	}

	@Test
	public void rewindsIntoABackwardYoyoIteration() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(2, 0.5F).start();
		tween.update(2.75F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertEquals(3, tween.getStep());
		tween.update(-0.5F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		Assert.assertEquals(2, tween.getStep());
	}

	@Test
	public void rewindsForeverWithAnInfiniteCount() {
		final List<String> events = new ArrayList<>();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(value).repeat(Tween.INFINITY, 0F), events).start();
		tween.update(0.5F);
		events.clear();
		tween.update(-1F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		Assert.assertEquals(Arrays.asList("onReverseIterationEnd", "onReverseIterationStart"), events);
	}

	@Test
	public void notifiesEveryForwardStep() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new MutableFloat(0F)), events).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onComplete"), events);
	}

	@Test
	public void notifiesEveryBackwardStep() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new MutableFloat(0F)), events).start();
		tween.update(2F);
		events.clear();
		tween.update(-3F);
		Assert.assertEquals(Arrays.asList("onReverseStart", "onReverseIterationStart", "onReverseIterationEnd", "onReverseComplete"), events);
	}

	@Test
	public void notifiesEachIterationOfItsRepetitions() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new MutableFloat(0F)).repeat(2, 0.5F), events).start();
		tween.update(5F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onComplete"), events);
	}

	@Test
	public void notifiesEachWayOfAYoyoForward() {
		final List<String> events = new ArrayList<>();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(value).repeatYoyo(1, 0F), events).start();
		tween.update(1.5F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart"), events);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(Arrays.asList("onStart", "onIterationStart", "onIterationEnd", "onIterationStart", "onIterationEnd", "onComplete"), events);
		Assert.assertEquals(0F, value.floatValue(), 0F);
	}

	@Test
	public void startsOnceAfterItsDelay() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new MutableFloat(0F)).delay(0.5F), events).start();
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
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).onStart(event -> events.add("start")).onIterationEnd(event -> events.add("end")).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("start", "end"), events);
	}

	@Test
	public void runsItsListenersInTheOrderOfTheirPriority() {
		final List<String> calls = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).onComplete(event -> calls.add("first")).onComplete(event -> calls.add("second")).onComplete(EventPriority.HIGH, event -> calls.add("high")).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("high", "first", "second"), calls);
	}

	@Test
	public void givesItselfInItsEvents() {
		final List<BaseTween<?>> starts = new ArrayList<>();
		final List<BaseTween<?>> ends = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).onIterationStart(event -> starts.add(event.getTween())).onIterationEnd(event -> ends.add(event.getTween())).start();
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
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(1, 0F).onIterationEnd(event -> {
			throw new IllegalStateException("boom");
		}).onIterationEnd(event -> calls.add("end")).onComplete(event -> calls.add("complete")).start();
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			tween.update(3F);
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals(Arrays.asList("end", "end", "complete"), calls);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] The onIterationEnd listener of Tween failed: java.lang.IllegalStateException: boom"));
		Assert.assertEquals(output.toString(), output.toString().indexOf("[JOID]"), output.toString().lastIndexOf("[JOID]"));
	}

	@Test
	public void forgetsItsListenersOnceFreed() {
		final List<String> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.record(BaseTweenTest.tween(new MutableFloat(0F)), events);
		tween.free();
		final Tween reused = BaseTweenTest.tween(new MutableFloat(0F)).start();
		reused.update(2F);
		Assert.assertSame(tween, reused);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void keepsItsUserData() {
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F));
		Assert.assertNull(tween.getUserData());
		Assert.assertSame(tween, tween.setUserData("data"));
		Assert.assertEquals("data", tween.getUserData());
	}

	@Test
	public void forcesItsEndValues() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).start();
		tween.update(0.25F);
		tween.forceToEnd(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		Assert.assertEquals(1, tween.getStep());
		Assert.assertEquals(0F, tween.getCurrentTime(), 0F);
	}

	@Test
	public void forcesTheStartValuesAtTheEndOfAYoyo() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeatYoyo(1, 0F).start();
		tween.update(0.25F);
		tween.forceToEnd(2F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertEquals(3, tween.getStep());
	}

	@Test
	public void forcesItsStartValues() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).delay(0.25F).start();
		tween.update(0.75F);
		tween.forceToStart();
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertEquals(-1, tween.getStep());
		Assert.assertEquals(-0.25F, tween.getCurrentTime(), 0F);
	}

	@Test
	public void drivesACustomTween() {
		final StepTween tween = new StepTween(1F).repeat(2, 0F).start();
		tween.update(3.5F);
		Assert.assertEquals(2, tween.starts);
		Assert.assertTrue(tween.isFinished());
		tween.free();
		Assert.assertTrue(tween.isStarted());
	}

	private static Tween tween(final MutableFloat value) {
		return Tween.to(value, 0, 1F).target(10F).ease(TweenEquations.LINEAR);
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
			this.duration = duration;
		}

		@Override
		protected void forceEndValues() {}

		@Override
		protected void forceStartValues() {
			this.starts++;
		}

		@Override
		protected boolean containsTarget(final Object target) {
			return false;
		}

		@Override
		protected boolean containsTarget(final Object target, final int tweenType) {
			return false;
		}

	}

}