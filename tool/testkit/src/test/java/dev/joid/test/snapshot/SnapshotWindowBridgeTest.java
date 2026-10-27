package dev.joid.test.snapshot;

import java.util.Collections;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.input.key.Key;

public class SnapshotWindowBridgeTest {

	@Test
	public void holdsTheKeysItPressesUntilItReleasesThem() {
		final SnapshotWindowBridge window = new SnapshotWindowBridge().press(Key.A, Key.LEFT_SHIFT);
		Assert.assertTrue(window.isKeyDown(Key.A));
		Assert.assertEquals(EnumSet.of(Key.A, Key.LEFT_SHIFT), window.getKeys());
		window.release(Key.LEFT_SHIFT, Key.B);
		Assert.assertEquals(Collections.singleton(Key.A), window.getKeys());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void givesItsKeysInReadOnly() {
		new SnapshotWindowBridge().getKeys().add(Key.A);
	}

}