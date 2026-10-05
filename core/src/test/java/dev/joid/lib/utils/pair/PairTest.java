package dev.joid.lib.utils.pair;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.utils.tuple.Tuple;

public class PairTest {

	@Test
	public void holdsTwoValuesOfTheSameType() {
		final Pair<String> pair = new Pair<>("left", "right");
		Assert.assertEquals("left", pair.getFirst());
		Assert.assertEquals("right", pair.getSecond());
	}

	@Test
	public void isATuple() {
		final Tuple<String, String> tuple = new Pair<>("left", "right");
		Assert.assertEquals("right", tuple.getSecond());
	}

}