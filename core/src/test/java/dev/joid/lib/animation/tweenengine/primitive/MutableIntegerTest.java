package dev.joid.lib.animation.tweenengine.primitive;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tweenengine.Tween;
import dev.joid.lib.animation.tweenengine.TweenEquations;

public class MutableIntegerTest {

	@Test
	public void convertsItsValue() {
		final MutableInteger value = new MutableInteger(3);
		Assert.assertEquals(3, value.intValue());
		Assert.assertEquals(3L, value.longValue());
		Assert.assertEquals(3F, value.floatValue(), 0F);
		Assert.assertEquals(3D, value.doubleValue(), 0D);
	}

	@Test
	public void changesItsValue() {
		final MutableInteger value = new MutableInteger(3);
		value.setValue(-7);
		Assert.assertEquals(-7, value.intValue());
	}

	@Test
	public void readsTheValueOfItsTarget() {
		final float[] values = new float[3];
		Assert.assertEquals(1, new MutableInteger(0).getValues(new MutableInteger(3), 0, values));
		Assert.assertEquals(3F, values[0], 0F);
	}

	@Test
	public void truncatesTheValueWrittenToItsTarget() {
		final MutableInteger accessor = new MutableInteger(0);
		final MutableInteger target = new MutableInteger(3);
		accessor.setValues(target, 0, new float[] {7.9F});
		Assert.assertEquals(7, target.intValue());
		Assert.assertEquals(0, accessor.intValue());
	}

	@Test
	public void animatesAsItsOwnAccessor() {
		final MutableInteger value = new MutableInteger(0);
		Tween.to(value, 0, 1F).target(10F).ease(TweenEquations.LINEAR).start().update(0.25F);
		Assert.assertEquals(2, value.intValue());
	}

}