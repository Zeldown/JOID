package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TweenTest {

	@Test
	public void describesItsInterpolation() {
		final Tween tween = new TweenValue(0F).to(2F);
		Assert.assertEquals(2F, tween.getDuration(), 0F);
		Assert.assertFalse(tween.isStarted());
	}

	@Test
	public void movesItsValueToItsTarget() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).target(10F).ease(TweenEquations.LINEAR).start();
		tween.update(0.5F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(10F, value.getValue(), 0F);
	}

	@Test
	public void readsItsStartWhenItBegins() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).target(10F).delay(1F).start();
		value.setValue(6F);
		tween.update(1F);
		tween.update(0.5F);
		Assert.assertEquals(8F, value.getValue(), 0F);
	}

	@Test
	public void easesWithItsEquation() {
		final TweenValue value = new TweenValue(0F);
		value.to(1F).target(10F).ease(TweenEquations.QUAD_INOUT).start().update(0.25F);
		Assert.assertEquals(1.25F, value.getValue(), 0F);
	}

	@Test
	public void movesLinearlyByDefault() {
		final TweenValue value = new TweenValue(0F);
		value.to(1F).target(10F).start().update(0.25F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
	}

	@Test
	public void replaysFromTheStartWhenStartedAgain() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).target(10F).start();
		tween.update(2F);
		Assert.assertTrue(tween.isFinished());
		value.setValue(4F);
		tween.start();
		Assert.assertFalse(tween.isFinished());
		Assert.assertEquals(-2, tween.getStep());
		tween.update(0.5F);
		Assert.assertEquals(7F, value.getValue(), 0F);
	}

	@Test
	public void setsItsTargetAtOnceWithoutDuration() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(0F).target(5F).start();
		tween.update(0F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		tween.update(0.25F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void restoresTheStartOfAnInstantTweenPlayedBackward() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(0F).target(5F).start();
		tween.update(0.25F);
		tween.update(-0.5F);
		Assert.assertEquals(0F, value.getValue(), 0F);
	}

	@Test
	public void setsItsTargetThenItsStartOnAYoyo() {
		final List<Float> values = new ArrayList<>();
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(0F).target(5F).repeatYoyo(1, 0F).onIterationStart(event -> values.add(value.getValue())).start();
		tween.update(0.25F);
		Assert.assertEquals(2, values.size());
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void marksAnEmptyStep() {
		final List<BaseTween<?>> events = new ArrayList<>();
		final Tween mark = Tween.mark().delay(0.5F).onStart(event -> events.add(event.getTween())).start();
		mark.update(1F);
		Assert.assertTrue(mark.isFinished());
		Assert.assertEquals(Collections.singletonList(mark), events);
	}

	@Test
	public void refusesANegativeDuration() {
		try {
			new TweenValue(0F).to(-1F);
			Assert.fail("A negative duration must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The duration of a tween cannot be negative, got -1.0: pass 0 or more", expected.getMessage());
		}
	}

	@Test
	public void takesAsManyWaypointsAsGiven() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).waypoint(10F).waypoint(20F).waypoint(30F).waypoint(40F).waypoint(50F).target(60F).path(TweenPaths.LINEAR).ease(TweenEquations.LINEAR).start();
		tween.update(0.5F);
		Assert.assertEquals(30F, value.getValue(), 1E-4F);
		tween.update(0.25F);
		Assert.assertEquals(45F, value.getValue(), 1E-4F);
	}

	@Test
	public void curvesThroughItsWaypoint() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).target(30F).waypoint(10F).ease(TweenEquations.LINEAR).start();
		tween.update(0.25F);
		Assert.assertEquals(3.75F, value.getValue(), 0.0001F);
		tween.update(0.25F);
		Assert.assertEquals(10F, value.getValue(), 0.0001F);
	}

	@Test
	public void followsItsPathThroughItsWaypoint() {
		final TweenValue value = new TweenValue(0F);
		final Tween tween = value.to(1F).target(30F).waypoint(10F).path(TweenPaths.LINEAR).ease(TweenEquations.LINEAR).start();
		tween.update(0.25F);
		Assert.assertEquals(5F, value.getValue(), 0F);
		tween.update(0.5F);
		Assert.assertEquals(20F, value.getValue(), 0F);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingPath() {
		new TweenValue(0F).to(1F).path(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingEquation() {
		new TweenValue(0F).to(1F).ease(null);
	}

}