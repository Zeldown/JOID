package dev.joid.lib.animation.tweenengine.primitive;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tweenengine.Tween;
import dev.joid.lib.animation.tweenengine.TweenEquations;

public class MutableFloatTest {

	@Test
	public void convertsItsValue() {
		final MutableFloat value = new MutableFloat(2.75F);
		Assert.assertEquals(2, value.intValue());
		Assert.assertEquals(2L, value.longValue());
		Assert.assertEquals(2.75F, value.floatValue(), 0F);
		Assert.assertEquals(2.75D, value.doubleValue(), 0D);
	}

	@Test
	public void changesItsValue() {
		final MutableFloat value = new MutableFloat(2.75F);
		value.setValue(-1.5F);
		Assert.assertEquals(-1.5F, value.floatValue(), 0F);
		Assert.assertEquals(-1, value.intValue());
	}

	@Test
	public void readsTheValueOfItsTarget() {
		final float[] values = new float[3];
		Assert.assertEquals(1, new MutableFloat(0F).getValues(new MutableFloat(2.75F), 0, values));
		Assert.assertEquals(2.75F, values[0], 0F);
		Assert.assertEquals(0F, values[1], 0F);
	}

	@Test
	public void writesTheValueOfItsTarget() {
		final MutableFloat accessor = new MutableFloat(0F);
		final MutableFloat target = new MutableFloat(2.75F);
		accessor.setValues(target, 0, new float[] {4.5F});
		Assert.assertEquals(4.5F, target.floatValue(), 0F);
		Assert.assertEquals(0F, accessor.floatValue(), 0F);
	}

	@Test
	public void animatesAsItsOwnAccessor() {
		final MutableFloat value = new MutableFloat(0F);
		Tween.to(value, 0, 1F).target(10F).ease(TweenEquations.LINEAR).start().update(0.25F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
	}

}