package dev.joid.lib.resource.animation;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ResourceAnimationTest {

	@Test
	public void findsTheFrameShownAtATime() {
		final ResourceAnimation animation = ResourceAnimation.of(1, 1, 0, Arrays.asList(ResourceAnimationFrame.of(new int[1], 50L), ResourceAnimationFrame.of(new int[1], 100L), ResourceAnimationFrame.of(new int[1], 150L)));
		Assert.assertEquals(300L, animation.getDuration());
		Assert.assertEquals(0, animation.indexAt(0L));
		Assert.assertEquals(0, animation.indexAt(49L));
		Assert.assertEquals(1, animation.indexAt(50L));
		Assert.assertEquals(1, animation.indexAt(149L));
		Assert.assertEquals(2, animation.indexAt(150L));
		Assert.assertEquals(2, animation.indexAt(1000L));
	}

	@Test
	public void keepsTheDurationOfItsShortFramesAboveZero() {
		final ResourceAnimation animation = ResourceAnimation.of(1, 1, 0, Arrays.asList(ResourceAnimationFrame.of(new int[1], 0L), ResourceAnimationFrame.of(new int[1], 5L), ResourceAnimationFrame.of(new int[1], 10L)));
		Assert.assertEquals(16L, animation.getDuration());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnAnimationWithoutFrames() {
		ResourceAnimation.of(1, 1, 0, Arrays.asList());
	}

}