package dev.joid.backend.lwjgl2.window.cursor;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.utils.platform.Platform;

public class NativeCursorTest {

	@Test
	public void choosesTheCursorsOfItsPlatform() {
		final NativeCursor cursor = NativeCursor.forPlatform();
		Assume.assumeNotNull(cursor);
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
		final WindowsNativeCursor cursor = new WindowsNativeCursor();
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
	public void appliesNothingWithoutDisplay() throws ReflectiveOperationException {
		final NativeCursor cursor = NativeCursor.forPlatform();
		Assume.assumeNotNull(cursor);
		Lwjgl2Natives.install();
		cursor.apply(Cursor.POINTER);
		Assert.assertSame(Cursor.DEFAULT, NativeCursorTest.read(cursor, "applied"));
		Assert.assertTrue(((Map<?, ?>) NativeCursorTest.read(cursor, "handleMap")).isEmpty());
	}

	private static Object read(final NativeCursor cursor, final String name) throws ReflectiveOperationException {
		final Field field = NativeCursor.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(cursor);
	}

}