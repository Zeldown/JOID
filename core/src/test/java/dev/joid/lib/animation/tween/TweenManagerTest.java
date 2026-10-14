package dev.joid.lib.animation.tween;

import org.junit.Assert;
import org.junit.Test;

public class TweenManagerTest {

	@Test
	public void startsWhatItReceives() {
		final TweenManager manager = new TweenManager();
		final Tween tween = Tween.mark();
		Assert.assertSame(manager, manager.add(tween));
		Assert.assertTrue(tween.isStarted());
		Assert.assertEquals(1, manager.size());
	}

	@Test
	public void addsAnObjectOnlyOnce() {
		final Tween tween = Tween.mark();
		Assert.assertEquals(1, new TweenManager().add(tween).add(tween).size());
	}

	@Test
	public void updatesEveryObject() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		new TweenManager().add(TweenManagerTest.tween(first, 10F)).add(TweenManagerTest.tween(second, 20F)).update(0.5F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void rewindsWithANegativeDelta() {
		final TweenValue value = new TweenValue(0F);
		final TweenManager manager = new TweenManager().add(value.to(2F).target(10F).ease(TweenEquations.LINEAR));
		manager.update(1F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		manager.update(-0.5F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
	}

	@Test
	public void freezesWhilePaused() {
		final TweenValue value = new TweenValue(0F);
		final TweenManager manager = new TweenManager().add(TweenManagerTest.tween(value, 10F));
		manager.pause();
		manager.update(0.5F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		manager.resume();
		manager.update(0.5F);
		Assert.assertEquals(5F, value.getValue(), 0F);
	}

	@Test
	public void removesItsFinishedObjects() {
		final Tween tween = TweenManagerTest.tween(new TweenValue(0F), 10F);
		final TweenManager manager = new TweenManager().add(tween);
		manager.update(2F);
		Assert.assertTrue(tween.isFinished());
		Assert.assertEquals(1, manager.size());
		manager.update(0F);
		Assert.assertEquals(0, manager.size());
	}

	@Test
	public void killsEveryObject() {
		final Tween tween = Tween.mark();
		final Timeline timeline = Timeline.createSequence();
		new TweenManager().add(tween).add(timeline).killAll();
		Assert.assertTrue(tween.isFinished());
		Assert.assertTrue(timeline.isFinished());
	}

	private static Tween tween(final TweenValue value, final float target) {
		return value.to(1F).target(target).ease(TweenEquations.LINEAR);
	}

}