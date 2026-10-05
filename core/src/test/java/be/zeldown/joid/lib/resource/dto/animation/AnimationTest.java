package be.zeldown.joid.lib.resource.dto.animation;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class AnimationTest {

	@Test
	public void findsTheFrameShownAtATime() {
		final Animation animation = Animation.create(1, 1, 0, Arrays.asList(AnimationFrame.create(new int[1], 50L), AnimationFrame.create(new int[1], 100L), AnimationFrame.create(new int[1], 150L)));
		Assert.assertEquals(300L, animation.getDuration());
		Assert.assertEquals(0, animation.indexAt(0L));
		Assert.assertEquals(0, animation.indexAt(49L));
		Assert.assertEquals(1, animation.indexAt(50L));
		Assert.assertEquals(1, animation.indexAt(149L));
		Assert.assertEquals(2, animation.indexAt(150L));
		Assert.assertEquals(2, animation.indexAt(1000L));
	}

	@Test
	public void slowsDownFramesTooShortToBeSeen() {
		final Animation animation = Animation.create(1, 1, 0, Arrays.asList(AnimationFrame.create(new int[1], 0L), AnimationFrame.create(new int[1], 5L), AnimationFrame.create(new int[1], 10L)));
		Assert.assertEquals(210L, animation.getDuration());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnAnimationWithoutFrames() {
		Animation.create(1, 1, 0, Arrays.asList());
	}

}