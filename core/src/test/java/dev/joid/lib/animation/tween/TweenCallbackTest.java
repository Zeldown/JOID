package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TweenCallbackTest {

	@Test
	public void givesEveryEventItsOwnFlag() {
		int all = 0;
		for (final int flag : new int[] {TweenCallback.BEGIN, TweenCallback.START, TweenCallback.END, TweenCallback.COMPLETE, TweenCallback.BACK_BEGIN, TweenCallback.BACK_START, TweenCallback.BACK_END, TweenCallback.BACK_COMPLETE}) {
			Assert.assertEquals(1, Integer.bitCount(flag));
			Assert.assertEquals(0, all & flag);
			all |= flag;
		}
		Assert.assertEquals(TweenCallback.ANY, all);
	}

	@Test
	public void groupsTheForwardAndBackwardEvents() {
		Assert.assertEquals(TweenCallback.BEGIN | TweenCallback.START | TweenCallback.END | TweenCallback.COMPLETE, TweenCallback.ANY_FORWARD);
		Assert.assertEquals(TweenCallback.BACK_BEGIN | TweenCallback.BACK_START | TweenCallback.BACK_END | TweenCallback.BACK_COMPLETE, TweenCallback.ANY_BACKWARD);
		Assert.assertEquals(TweenCallback.ANY_FORWARD | TweenCallback.ANY_BACKWARD, TweenCallback.ANY);
	}

	@Test
	public void runsTheNextCallbackAfterItself() {
		final List<Object> calls = new ArrayList<>();
		final Tween source = Tween.mark();
		final TweenCallback first = (type, tween) -> calls.add(type);
		final TweenCallback second = (type, tween) -> calls.add(tween);
		first.andThen(second).onEvent(TweenCallback.END, source);
		Assert.assertEquals(Arrays.asList(TweenCallback.END, source), calls);
	}

}