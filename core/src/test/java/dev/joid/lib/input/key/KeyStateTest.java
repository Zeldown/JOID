package dev.joid.lib.input.key;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;

public class KeyStateTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void holdsTheLayoutLetterAndThePhysicalKeyOnAnAzertyKeyboard() {
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.Q, Key.LEFT_CONTROL));
		BridgeHandler.UI.keyPressed(Key.Q, Key.A);
		Assert.assertTrue(Key.A.isDown());
		Assert.assertFalse(Key.Q.isDown());
		Assert.assertTrue(Key.Q.isPhysicalDown());
		Assert.assertFalse(Key.A.isPhysicalDown());
		Assert.assertTrue(Key.LEFT_CONTROL.isDown());
		Assert.assertTrue(Key.LEFT_CONTROL.isPhysicalDown());
	}

	@Test
	public void releasesTheKeyGivenAtThePress() {
		final KeyState state = KeyState.create();
		state.press(Key.Q, Key.A);
		state.press(Key.W, Key.Z);
		Assert.assertSame(Key.A, state.release(Key.Q));
		Assert.assertSame(Key.Z, state.release(Key.W));
	}

	@Test
	public void releasesThePositionOfAKeyNeverPressed() {
		final KeyState state = KeyState.create();
		Assert.assertSame(Key.Q, state.release(Key.Q));
		state.press(Key.Q, Key.A);
		state.release(Key.Q);
		Assert.assertSame(Key.Q, state.release(Key.Q));
	}

	@Test
	public void holdsTheLetterWhileTheWindowHoldsItsPosition() {
		this.bridges.getWindow().getKeys().add(Key.Q);
		BridgeHandler.UI.keyPressed(Key.Q, Key.A);
		this.bridges.getWindow().getKeys().clear();
		Assert.assertFalse(Key.A.isDown());
		this.bridges.getWindow().getKeys().add(Key.Q);
		Assert.assertTrue(Key.A.isDown());
	}

	@Test
	public void releasesTheLetterOfAPressShorterThanAFrame() {
		BridgeHandler.UI.keyPressed(Key.Q, Key.A);
		Assert.assertFalse(Key.A.isDown());
		Assert.assertSame(Key.A, BridgeHandler.UI.getKeyState().release(Key.Q));
	}

	@Test
	public void readsAKeyOutsideTheLayoutByItsPosition() {
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_SHIFT, Key.UP, Key.F5, Key.A));
		Assert.assertTrue(Key.LEFT_SHIFT.isDown());
		Assert.assertTrue(Key.UP.isDown());
		Assert.assertTrue(Key.F5.isDown());
		Assert.assertFalse(Key.A.isDown());
	}

}