package dev.joid.lib.animation.animator;

import org.junit.Assert;
import org.junit.Test;

public class TweenAnimatorAccessorTest {

	@Test
	public void readsTheValueOfAnAnimator() {
		final float[] values = new float[3];
		Assert.assertEquals(1, new TweenAnimatorAccessor().getValues(TweenAnimator.create(4F), TweenAnimatorAccessor.ANIMATION_VALUE, values));
		Assert.assertEquals(4F, values[0], 0F);
	}

	@Test
	public void writesTheValueOfAnAnimator() {
		final TweenAnimator animator = TweenAnimator.create(4F);
		new TweenAnimatorAccessor().setValues(animator, TweenAnimatorAccessor.ANIMATION_VALUE, new float[] {7F});
		Assert.assertEquals(7F, animator.getValue(), 0F);
	}

	@Test
	public void ignoresAnUnknownType() {
		final TweenAnimator animator = TweenAnimator.create(4F);
		final float[] values = {9F};
		new TweenAnimatorAccessor().getValues(animator, 5, values);
		new TweenAnimatorAccessor().setValues(animator, 5, new float[] {7F});
		Assert.assertEquals(9F, values[0], 0F);
		Assert.assertEquals(4F, animator.getValue(), 0F);
	}

}