package dev.joid.lib.animation.tweenengine;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tweenengine.primitive.MutableFloat;

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
	public void leavesAnObjectIdleWithoutAutoStart() {
		final Tween tween = Tween.mark();
		TweenManager.setAutoStart(tween, false);
		new TweenManager().add(tween);
		Assert.assertFalse(tween.isStarted());
	}

	@Test
	public void updatesEveryObject() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		new TweenManager().add(TweenManagerTest.tween(first, 10F)).add(TweenManagerTest.tween(second, 20F)).update(0.5F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
	}

	@Test
	public void rewindsWithANegativeDelta() {
		final MutableFloat value = new MutableFloat(0F);
		final TweenManager manager = new TweenManager().add(Tween.to(value, 0, 2F).target(10F).ease(TweenEquations.LINEAR));
		manager.update(1F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		manager.update(-0.5F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
	}

	@Test
	public void freezesWhilePaused() {
		final MutableFloat value = new MutableFloat(0F);
		final TweenManager manager = new TweenManager().add(TweenManagerTest.tween(value, 10F));
		manager.pause();
		manager.update(0.5F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		manager.resume();
		manager.update(0.5F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
	}

	@Test
	public void freesItsFinishedObjects() {
		final Tween tween = TweenManagerTest.tween(new MutableFloat(0F), 10F);
		final TweenManager manager = new TweenManager().add(tween);
		manager.update(2F);
		Assert.assertTrue(tween.isFinished());
		Assert.assertEquals(1, manager.size());
		manager.update(0F);
		Assert.assertEquals(0, manager.size());
		Assert.assertNull(tween.getTarget());
	}

	@Test
	public void keepsItsFinishedObjectsWithoutAutoRemove() {
		final Tween tween = TweenManagerTest.tween(new MutableFloat(0F), 10F);
		TweenManager.setAutoRemove(tween, false);
		final TweenManager manager = new TweenManager().add(tween);
		manager.update(2F);
		manager.update(0F);
		Assert.assertEquals(1, manager.size());
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void findsTheTargetsOfItsObjects() {
		final MutableFloat value = new MutableFloat(0F);
		final MutableFloat other = new MutableFloat(0F);
		final TweenManager manager = new TweenManager().add(Tween.to(value, 1, 1F)).add(Timeline.createSequence().push(Tween.to(other, 2, 1F)));
		Assert.assertTrue(manager.containsTarget(value));
		Assert.assertTrue(manager.containsTarget(other));
		Assert.assertFalse(manager.containsTarget(new MutableFloat(0F)));
	}

	@Test
	public void findsTheTargetsOfItsObjectsByType() {
		final MutableFloat value = new MutableFloat(0F);
		final MutableFloat other = new MutableFloat(0F);
		final TweenManager manager = new TweenManager().add(Tween.to(value, 1, 1F)).add(Timeline.createSequence().push(Tween.to(other, 2, 1F)));
		Assert.assertTrue(manager.containsTarget(value, 1));
		Assert.assertFalse(manager.containsTarget(value, 2));
		Assert.assertTrue(manager.containsTarget(other, 2));
		Assert.assertFalse(manager.containsTarget(other, 1));
	}

	@Test
	public void killsEveryObject() {
		final Tween tween = Tween.mark();
		final Timeline timeline = Timeline.createSequence();
		new TweenManager().add(tween).add(timeline).killAll();
		Assert.assertTrue(tween.isFinished());
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void killsTheObjectsOfATarget() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 1, 1F);
		final Tween other = Tween.to(new MutableFloat(0F), 1, 1F);
		final Timeline timeline = Timeline.createSequence().push(Tween.to(value, 2, 1F));
		final Timeline untouched = Timeline.createSequence().push(Tween.to(new MutableFloat(0F), 2, 1F));
		new TweenManager().add(tween).add(other).add(timeline).add(untouched).killTarget(value);
		Assert.assertTrue(tween.isFinished());
		Assert.assertFalse(other.isFinished());
		Assert.assertTrue(timeline.isFinished());
		Assert.assertFalse(untouched.isFinished());
	}

	@Test
	public void killsTheObjectsOfATargetAndType() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 1, 1F);
		final Tween other = Tween.to(value, 2, 1F);
		final Timeline timeline = Timeline.createSequence().push(Tween.to(value, 1, 1F));
		final Timeline untouched = Timeline.createSequence().push(Tween.to(value, 2, 1F));
		new TweenManager().add(tween).add(other).add(timeline).add(untouched).killTarget(value, 1);
		Assert.assertTrue(tween.isFinished());
		Assert.assertFalse(other.isFinished());
		Assert.assertTrue(timeline.isFinished());
		Assert.assertFalse(untouched.isFinished());
	}

	@Test
	public void countsItsRunningTweensAndTimelines() {
		final Timeline inner = Timeline.createParallel().push(Tween.mark());
		final TweenManager manager = new TweenManager().add(Tween.mark()).add(Timeline.createSequence().push(Tween.mark()).push(Tween.mark()).push(inner));
		Assert.assertEquals(4, manager.getRunningTweensCount());
		Assert.assertEquals(2, manager.getRunningTimelinesCount());
	}

	@Test
	public void listsACopyOfItsObjects() {
		final Tween tween = Tween.mark();
		final TweenManager manager = new TweenManager().add(tween);
		final List<BaseTween<?>> objects = manager.getObjects();
		manager.add(Tween.mark());
		Assert.assertEquals(Collections.singletonList(tween), objects);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyObjects() {
		new TweenManager().getObjects().add(Tween.mark());
	}

	@Test
	public void reservesRoomWithoutAddingObjects() {
		final TweenManager manager = new TweenManager();
		manager.ensureCapacity(50);
		Assert.assertEquals(0, manager.size());
	}

	@Test
	public void countsTheTweensOfANestedSequence() {
		final TweenManager manager = new TweenManager().add(Timeline.createSequence().push(Tween.mark()).beginSequence().push(Tween.mark()).push(Tween.mark()).end());
		Assert.assertEquals(3, manager.getRunningTweensCount());
	}

	@Test
	public void countsTheTimelinesOfANestedParallel() {
		final TweenManager manager = new TweenManager().add(Timeline.createSequence().push(Tween.mark()).beginParallel().push(Tween.mark()).end());
		Assert.assertEquals(2, manager.getRunningTimelinesCount());
	}

	private static Tween tween(final MutableFloat value, final float target) {
		return Tween.to(value, 0, 1F).target(target).ease(TweenEquations.LINEAR);
	}

}