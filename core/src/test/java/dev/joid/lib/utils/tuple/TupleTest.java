package dev.joid.lib.utils.tuple;

import org.junit.Assert;
import org.junit.Test;

public class TupleTest {

	@Test
	public void holdsTwoValuesOfAnyType() {
		final Tuple<String, Integer> tuple = new Tuple<>("width", 42);
		Assert.assertEquals("width", tuple.getFirst());
		Assert.assertEquals(42, tuple.getSecond().intValue());
	}

}