package dev.joid.backend.lwjgl2.window.cursor;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import dev.joid.backend.lwjgl2.Natives;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.utils.platform.Platform;

public class NativeCursorTest {

	@Test
	public void choosesTheCursorsOfItsPlatform() {
		final NativeCursor cursor = NativeCursors.get();
		Assume.assumeNotNull(cursor);
		Assert.assertSame(cursor, NativeCursors.get());
		switch (Platform.current()) {
		case WINDOWS:
			Assert.assertTrue(cursor instanceof WindowsNativeCursor);
			break;
		case MACOS:
			Assert.assertTrue(cursor instanceof MacNativeCursor);
			break;
		default:
			Assert.assertTrue(cursor instanceof X11NativeCursor);
			break;
		}
	}

	@Test
	public void loadsADistinctWindowsHandleForEveryCursor() throws ReflectiveOperationException {
		Assume.assumeTrue(Platform.current() == Platform.WINDOWS);
		final WindowsNativeCursor cursor = WindowsNativeCursor.create();
		final Set<Long> handles = new HashSet<>();
		for (final Cursor shape : Cursor.values()) {
			final ByteBuffer handle = (ByteBuffer) cursor.load(shape);
			Assert.assertTrue(handle.isDirect());
			final long value = handle.capacity() == 8 ? handle.getLong(0) : handle.getInt(0);
			Assert.assertNotEquals(0L, value);
			Assert.assertTrue(shape + " shares its handle", handles.add(value));
		}
	}

	@Test
	public void appliesNothingWithoutDisplay() {
		final NativeCursor cursor = NativeCursors.get();
		Assume.assumeNotNull(cursor);
		Natives.install();
		cursor.apply(Cursor.POINTER);
		cursor.apply(Cursor.DEFAULT);
	}

}