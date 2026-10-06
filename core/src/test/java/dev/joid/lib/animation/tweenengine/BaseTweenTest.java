package dev.joid.lib.animation.tweenengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tweenengine.primitive.MutableFloat;

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
		final List<Integer> events = new ArrayList<>();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = BaseTweenTest.tween(value).repeat(Tween.INFINITY, 0F).setCallback((type, source) -> events.add(type)).setCallbackTriggers(TweenCallback.ANY_BACKWARD).start();
		tween.update(0.5F);
		tween.update(-1F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertFalse(tween.isFinished());
		Assert.assertEquals(Arrays.asList(TweenCallback.BACK_END, TweenCallback.BACK_START), events);
	}

	@Test
	public void notifiesEveryForwardStep() {
		final List<Integer> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).setCallback((type, source) -> events.add(type)).setCallbackTriggers(TweenCallback.ANY).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList(TweenCallback.BEGIN, TweenCallback.START, TweenCallback.END, TweenCallback.COMPLETE), events);
	}

	@Test
	public void notifiesEveryBackwardStep() {
		final List<Integer> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).setCallback((type, source) -> events.add(type)).setCallbackTriggers(TweenCallback.ANY).start();
		tween.update(2F);
		events.clear();
		tween.update(-3F);
		Assert.assertEquals(Arrays.asList(TweenCallback.BACK_BEGIN, TweenCallback.BACK_START, TweenCallback.BACK_END, TweenCallback.BACK_COMPLETE), events);
	}

	@Test
	public void notifiesOnlyItsCompletionByDefault() {
		final List<Integer> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).setCallback((type, source) -> events.add(type)).start();
		tween.update(2F);
		Assert.assertEquals(Collections.singletonList(TweenCallback.COMPLETE), events);
	}

	@Test
	public void notifiesOnlyTheChosenTriggers() {
		final List<Integer> events = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).setCallback((type, source) -> events.add(type)).setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList(TweenCallback.BEGIN, TweenCallback.END), events);
	}

	@Test
	public void chainsItsCallbacks() {
		final List<String> calls = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).setCallback((type, source) -> calls.add("first")).setCallback((type, source) -> calls.add("second")).start();
		tween.update(2F);
		Assert.assertEquals(Arrays.asList("first", "second"), calls);
	}

	@Test
	public void passesItselfToAnAddedCallback() {
		final List<BaseTween<?>> starts = new ArrayList<>();
		final List<BaseTween<?>> ends = new ArrayList<>();
		final Tween tween = BaseTweenTest.tween(new MutableFloat(0F)).addCallback(TweenCallback.START, starts::add).addCallback(TweenCallback.END, ends::add).start();
		tween.update(0.5F);
		Assert.assertEquals(Collections.singletonList(tween), starts);
		Assert.assertTrue(ends.isEmpty());
		tween.update(1F);
		Assert.assertEquals(Collections.singletonList(tween), starts);
		Assert.assertEquals(Collections.singletonList(tween), ends);
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