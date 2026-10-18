package dev.joid.lib.signal.replay;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class SignalReplayPartsTest {

	@Test
	public void solvesARepeatedHoleOnlyWhenItsValuesMatch() {
		final SignalReplayParts parts = new SignalReplayParts();
		parts.addHole("12:0", "name");
		parts.add(" ");
		parts.addHole("12:0", "name");
		Assert.assertEquals(Collections.singletonMap("12:0", "Ada"), parts.solve("Ada Ada"));
		Assert.assertNull(parts.solve("Ada Grace"));
	}

}