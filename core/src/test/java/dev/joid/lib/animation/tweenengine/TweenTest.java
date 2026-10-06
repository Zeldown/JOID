package dev.joid.lib.animation.tweenengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.animator.TweenAnimatorAccessor;
import dev.joid.lib.animation.tweenengine.primitive.MutableFloat;

import lombok.AllArgsConstructor;

public class TweenTest {

	@Before
	public void registerThePointAccessor() {
		Tween.registerAccessor(Point.class, new PointAccessor());
	}

	@After
	public void restoreTheLimits() {
		Tween.setWaypointsLimit(0);
		Tween.setCombinedAttributesLimit(3);
	}

	@Test
	public void describesItsInterpolation() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 4, 2F);
		Assert.assertSame(value, tween.getTarget());
		Assert.assertEquals(4, tween.getType());
		Assert.assertEquals(2F, tween.getDuration(), 0F);
		Assert.assertSame(TweenEquations.QUAD_INOUT, tween.getEasing());
		Assert.assertSame(MutableFloat.class, tween.getTargetClass());
	}

	@Test
	public void movesItsTargetToItsValue() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 0, 1F).target(10F).ease(TweenEquations.LINEAR).start();
		tween.update(0.5F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		Assert.assertEquals(10F, tween.getTargetValues()[0], 0F);
	}

	@Test
	public void easesWithItsEquation() {
		final MutableFloat value = new MutableFloat(0F);
		Tween.to(value, 0, 1F).target(10F).start().update(0.25F);
		Assert.assertEquals(1.25F, value.floatValue(), 0F);
	}

	@Test
	public void movesItsTargetFromItsValue() {
		final MutableFloat value = new MutableFloat(10F);
		final Tween tween = Tween.from(value, 0, 1F).target(0F).ease(TweenEquations.LINEAR).start();
		tween.update(0.25F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
	}

	@Test
	public void setsItsTargetAtOnce() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.set(value, 0).target(5F).start();
		Assert.assertEquals(0F, tween.getDuration(), 0F);
		tween.update(0F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		tween.update(0.25F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void restoresTheValueOfASetPlayedBackward() {
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.set(value, 0).target(5F).start();
		tween.update(0.25F);
		tween.update(-0.5F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
	}

	@Test
	public void setsItsTargetThenItsStartOnAYoyo() {
		final List<Float> values = new ArrayList<>();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.set(value, 0).target(5F).repeatYoyo(1, 0F).setCallback((type, source) -> values.add(value.floatValue())).setCallbackTriggers(TweenCallback.START).start();
		tween.update(0.25F);
		Assert.assertEquals(Arrays.asList(0F, 5F), values);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertTrue(tween.isFinished());
	}

	@Test
	public void callsItsCallbackOnStart() {
		final List<Integer> events = new ArrayList<>();
		final Tween tween = Tween.call((type, source) -> events.add(type)).start();
		Assert.assertNull(tween.getTarget());
		Assert.assertEquals(-1, tween.getType());
		tween.update(1F);
		Assert.assertEquals(Collections.singletonList(TweenCallback.START), events);
	}

	@Test
	public void marksAnEmptyStep() {
		final Tween mark = Tween.mark().delay(0.5F).start();
		Assert.assertNull(mark.getTarget());
		Assert.assertEquals(0.5F, mark.getFullDuration(), 0F);
		mark.update(1F);
		Assert.assertTrue(mark.isFinished());
	}

	@Test(expected = RuntimeException.class)
	public void refusesANegativeDuration() {
		Tween.to(new MutableFloat(0F), 0, -1F);
	}

	@Test
	public void combinesTwoAttributes() {
		final Point point = new Point(0F, 0F, 0F);
		final Tween tween = Tween.to(point, 2, 1F).target(10F, 20F).ease(TweenEquations.LINEAR).start();
		tween.update(0.5F);
		Assert.assertEquals(2, tween.getCombinedAttributesCount());
		Assert.assertEquals(5F, point.x, 0F);
		Assert.assertEquals(10F, point.y, 0F);
		Assert.assertEquals(0F, point.z, 0F);
	}

	@Test
	public void combinesThreeAttributes() {
		final Point point = new Point(0F, 0F, 0F);
		Tween.to(point, 3, 1F).target(10F, 20F, 30F).ease(TweenEquations.LINEAR).start().update(0.5F);
		Assert.assertEquals(5F, point.x, 0F);
		Assert.assertEquals(10F, point.y, 0F);
		Assert.assertEquals(15F, point.z, 0F);
	}

	@Test
	public void combinesAnArrayOfAttributes() {
		final Point point = new Point(0F, 0F, 0F);
		Tween.to(point, 3, 1F).target(new float[] {10F, 20F, 30F}).ease(TweenEquations.LINEAR).start().update(0.5F);
		Assert.assertEquals(5F, point.x, 0F);
		Assert.assertEquals(10F, point.y, 0F);
		Assert.assertEquals(15F, point.z, 0F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesMoreAttributesThanTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 3, 1F).target(1F, 2F, 3F, 4F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesAnAccessorCombiningMoreAttributesThanTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 4, 1F).start();
	}

	@Test
	public void raisesTheCombinedAttributesLimit() {
		Tween.setCombinedAttributesLimit(4);
		TweenTest.emptyThePool();
		final Tween tween = Tween.to(new Point(0F, 0F, 0F), 4, 1F).target(1F, 2F, 3F, 4F).start();
		Assert.assertEquals(4, tween.getCombinedAttributesCount());
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, tween.getTargetValues(), 0F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesMoreAttributesThanALoweredLimit() {
		Tween.mark().free();
		Tween.setCombinedAttributesLimit(2);
		Tween.to(new MutableFloat(0F), 0, 1F).target(new float[] {1F, 2F, 3F});
	}

	@Test
	public void addsTheRelativeTargetToTheStartValue() {
		final MutableFloat value = new MutableFloat(5F);
		Tween.to(value, 0, 1F).targetRelative(10F).ease(TweenEquations.LINEAR).start().update(2F);
		Assert.assertEquals(15F, value.floatValue(), 0F);
	}

	@Test
	public void addsEveryRelativeTargetToTheStartValues() {
		final Point pair = new Point(1F, 2F, 3F);
		final Point triple = new Point(1F, 2F, 3F);
		final Point array = new Point(1F, 2F, 3F);
		Tween.to(pair, 2, 1F).targetRelative(10F, 20F).start().update(2F);
		Tween.to(triple, 3, 1F).targetRelative(10F, 20F, 30F).start().update(2F);
		Tween.to(array, 3, 1F).targetRelative(new float[] {10F, 20F, 30F}).start().update(2F);
		Assert.assertEquals(11F, pair.x, 0F);
		Assert.assertEquals(22F, pair.y, 0F);
		Assert.assertEquals(3F, pair.z, 0F);
		for (final Point point : new Point[] {triple, array}) {
			Assert.assertEquals(11F, point.x, 0F);
			Assert.assertEquals(22F, point.y, 0F);
			Assert.assertEquals(33F, point.z, 0F);
		}
	}

	@Test
	public void addsALateRelativeTargetToTheStartValue() {
		final MutableFloat value = new MutableFloat(5F);
		final Tween tween = Tween.to(value, 0, 1F).target(5F).start();
		tween.update(0F);
		tween.targetRelative(10F).update(2F);
		Assert.assertEquals(15F, value.floatValue(), 0F);
	}

	@Test
	public void addsEveryLateRelativeTargetToTheStartValues() {
		final Point pair = new Point(1F, 2F, 3F);
		final Point triple = new Point(1F, 2F, 3F);
		final Point array = new Point(1F, 2F, 3F);
		final Tween pairTween = Tween.to(pair, 2, 1F).start();
		final Tween tripleTween = Tween.to(triple, 3, 1F).start();
		final Tween arrayTween = Tween.to(array, 3, 1F).start();
		pairTween.update(0F);
		tripleTween.update(0F);
		arrayTween.update(0F);
		pairTween.targetRelative(10F, 20F).update(2F);
		tripleTween.targetRelative(10F, 20F, 30F).update(2F);
		arrayTween.targetRelative(new float[] {10F, 20F, 30F}).update(2F);
		Assert.assertEquals(11F, pair.x, 0F);
		Assert.assertEquals(22F, pair.y, 0F);
		Assert.assertEquals(3F, pair.z, 0F);
		for (final Point point : new Point[] {triple, array}) {
			Assert.assertEquals(11F, point.x, 0F);
			Assert.assertEquals(22F, point.y, 0F);
			Assert.assertEquals(33F, point.z, 0F);
		}
	}

	@Test(expected = RuntimeException.class)
	public void refusesMoreRelativeAttributesThanTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 3, 1F).targetRelative(1F, 2F, 3F, 4F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesAWaypointBeyondTheLimit() {
		Tween.to(new MutableFloat(0F), 0, 1F).waypoint(5F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesAPairWaypointBeyondTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 2, 1F).waypoint(5F, 5F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesATripleWaypointBeyondTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 3, 1F).waypoint(5F, 5F, 5F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesAnArrayWaypointBeyondTheLimit() {
		Tween.to(new Point(0F, 0F, 0F), 3, 1F).waypoint(new float[] {5F, 5F, 5F});
	}

	@Test
	public void curvesThroughItsWaypoint() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 0, 1F).target(30F).waypoint(10F).ease(TweenEquations.LINEAR).start();
		tween.update(0.25F);
		Assert.assertEquals(3.75F, value.floatValue(), 0.0001F);
		tween.update(0.25F);
		Assert.assertEquals(10F, value.floatValue(), 0.0001F);
	}

	@Test
	public void followsItsPathThroughItsWaypoint() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final MutableFloat value = new MutableFloat(0F);
		final Tween tween = Tween.to(value, 0, 1F).target(30F).waypoint(10F).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start();
		tween.update(0.25F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
		tween.update(0.5F);
		Assert.assertEquals(20F, value.floatValue(), 0F);
	}

	@Test
	public void ignoresItsWaypointsWithoutPath() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final MutableFloat value = new MutableFloat(0F);
		Tween.to(value, 0, 1F).target(30F).waypoint(10F).path(null).ease(TweenEquations.LINEAR).start().update(0.5F);
		Assert.assertEquals(15F, value.floatValue(), 0F);
	}

	@Test
	public void movesEveryAttributeThroughItsWaypoints() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final Point pair = new Point(0F, 0F, 0F);
		final Point triple = new Point(0F, 0F, 0F);
		final Point array = new Point(0F, 0F, 0F);
		Tween.to(pair, 2, 1F).target(20F, 40F).waypoint(2F, 4F).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start().update(0.5F);
		Tween.to(triple, 3, 1F).target(20F, 40F, 60F).waypoint(2F, 4F, 6F).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start().update(0.5F);
		Tween.to(array, 3, 1F).target(20F, 40F, 60F).waypoint(new float[] {2F, 4F, 6F}).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start().update(0.5F);
		Assert.assertEquals(2F, pair.x, 0F);
		Assert.assertEquals(4F, pair.y, 0F);
		for (final Point point : new Point[] {triple, array}) {
			Assert.assertEquals(2F, point.x, 0F);
			Assert.assertEquals(4F, point.y, 0F);
			Assert.assertEquals(6F, point.z, 0F);
		}
	}

	@Test
	public void shiftsItsWaypointsWithARelativeTarget() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final MutableFloat value = new MutableFloat(5F);
		final Tween tween = Tween.to(value, 0, 1F).targetRelative(20F).waypoint(10F).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start();
		tween.update(0.5F);
		Assert.assertEquals(15F, value.floatValue(), 0F);
		tween.update(1F);
		Assert.assertEquals(25F, value.floatValue(), 0F);
	}

	@Test
	public void findsTheAccessorOfAParentClass() {
		final Tween tween = Tween.to(new Pixel(), 1, 1F);
		Assert.assertSame(Point.class, tween.getTargetClass());
		Assert.assertSame(Tween.getRegisteredAccessor(Point.class).get(), tween.build().getAccessor());
	}

	@Test
	public void findsTheAccessorOfAnAncestorClass() {
		Assert.assertSame(Point.class, Tween.to(new Pixel() {}, 1, 1F).getTargetClass());
	}

	@Test
	public void usesItsTargetAsItsOwnAccessor() {
		final MutableFloat value = new MutableFloat(0F);
		Assert.assertSame(value, Tween.to(value, 0, 1F).build().getAccessor());
	}

	@Test(expected = RuntimeException.class)
	public void refusesATargetWithoutAccessor() {
		Tween.to(new Object(), 0, 1F).start();
	}

	@Test
	public void registersOneAccessorPerClass() {
		final PointAccessor accessor = new PointAccessor();
		Tween.registerAccessor(Point.class, accessor);
		Assert.assertSame(accessor, Tween.getRegisteredAccessor(Point.class).get());
		Assert.assertFalse(Tween.getRegisteredAccessor(Pixel.class).isPresent());
		Assert.assertTrue(Tween.getRegisteredAccessor(TweenAnimator.class).get() instanceof TweenAnimatorAccessor);
	}

	@Test
	public void castsItsTargetClass() {
		Assert.assertSame(Pixel.class, Tween.to(new Pixel(), 1, 1F).cast(Pixel.class).getTargetClass());
	}

	@Test(expected = RuntimeException.class)
	public void looksTheAccessorUpByItsCastClass() {
		Tween.to(new Pixel(), 1, 1F).cast(Pixel.class).start();
	}

	@Test(expected = RuntimeException.class)
	public void refusesToCastOnceStarted() {
		Tween.to(new Point(0F, 0F, 0F), 1, 1F).start().cast(Point.class);
	}

	@Test
	public void returnsToThePoolOnceFreed() {
		final Tween tween = Tween.to(new MutableFloat(0F), 0, 1F);
		final int size = Tween.getPoolSize();
		tween.free();
		Assert.assertEquals(size + 1, Tween.getPoolSize());
		Assert.assertNull(tween.getTarget());
		tween.free();
		Assert.assertEquals(size + 1, Tween.getPoolSize());
	}

	@Test
	public void reusesAFreedTween() {
		final Tween tween = Tween.mark();
		tween.free();
		Assert.assertSame(tween, Tween.mark());
	}

	@Test
	public void reservesRoomInThePoolWithoutFillingIt() {
		final int size = Tween.getPoolSize();
		Tween.ensurePoolCapacity(size + 50);
		Assert.assertEquals(size, Tween.getPoolSize());
	}

	@Test
	public void tellsItsVersion() {
		Assert.assertEquals("6.3.3", Tween.getVersion());
	}

	@Test
	public void acceptsAWaypointOnAPooledTweenOnceTheLimitIsRaised() {
		TweenTest.emptyThePool();
		Tween.mark().free();
		Tween.setWaypointsLimit(1);
		final MutableFloat value = new MutableFloat(0F);
		Tween.to(value, 0, 1F).target(30F).waypoint(10F).path(TweenPaths.linear).ease(TweenEquations.LINEAR).start().update(0.5F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
	}

	@Test
	public void acceptsMoreAttributesOnAPooledTweenOnceTheLimitIsRaised() {
		TweenTest.emptyThePool();
		Tween.mark().free();
		Tween.setCombinedAttributesLimit(4);
		final Tween tween = Tween.to(new MutableFloat(0F), 0, 1F).target(1F, 2F, 3F, 4F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, tween.getTargetValues(), 0F);
	}

	@Test
	public void explainsTheCombinedAttributesLimitItReached() {
		final String message = "You cannot combine more than 3 attributes in a tween. You can raise this limit with Tween.setCombinedAttributesLimit(), which should be called once in application initialization code.";
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 3, 1F).target(1F, 2F, 3F, 4F)));
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 3, 1F).targetRelative(1F, 2F, 3F, 4F)));
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 4, 1F).start()));
	}

	@Test
	public void explainsTheWaypointsLimitItReached() {
		Tween.setWaypointsLimit(1);
		TweenTest.emptyThePool();
		final String message = "You cannot add more than 1 waypoints to a tween. You can raise this limit with Tween.setWaypointsLimit(), which should be called once in application initialization code.";
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new MutableFloat(0F), 0, 1F).waypoint(5F).waypoint(6F)));
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 2, 1F).waypoint(5F, 5F).waypoint(6F, 6F)));
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 3, 1F).waypoint(5F, 5F, 5F).waypoint(6F, 6F, 6F)));
		Assert.assertEquals(message, TweenTest.refusal(() -> Tween.to(new Point(0F, 0F, 0F), 3, 1F).waypoint(new float[] {5F, 5F, 5F}).waypoint(new float[] {6F, 6F, 6F})));
	}

	private static void emptyThePool() {
		while (Tween.getPoolSize() > 0) {
			Tween.mark();
		}
	}

	private static String refusal(final Runnable action) {
		try {
			action.run();
		} catch (final RuntimeException expected) {
			return expected.getMessage();
		}
		throw new AssertionError("The tween must be refused");
	}

	@AllArgsConstructor
	private static class Point {

		private float x;
		private float y;
		private float z;

	}

	private static class Pixel extends Point {

		private Pixel() {
			super(0F, 0F, 0F);
		}

	}

	private static final class PointAccessor implements TweenAccessor<Point> {

		@Override
		public int getValues(final Point target, final int tweenType, final float[] returnValues) {
			returnValues[0] = target.x;
			if (tweenType > 1) {
				returnValues[1] = target.y;
			}

			if (tweenType > 2) {
				returnValues[2] = target.z;
			}

			return tweenType;
		}

		@Override
		public void setValues(final Point target, final int tweenType, final float[] newValues) {
			target.x = newValues[0];
			if (tweenType > 1) {
				target.y = newValues[1];
			}

			if (tweenType > 2) {
				target.z = newValues[2];
			}
		}

	}

}