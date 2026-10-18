package dev.joid.lib.render.tessellator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TessellatorAccess {

	public static Tessellator create() {
		try {
			final Constructor<Tessellator> constructor = Tessellator.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	public static int[] rawBuffer(final Tessellator tessellator) {
		final int[] buffer = TessellatorAccess.read(tessellator, "rawBuffer");
		return buffer == null ? null : buffer.clone();
	}

	public static int rawBufferSize(final Tessellator tessellator) {
		return TessellatorAccess.<Integer>read(tessellator, "rawBufferSize");
	}

	public static int vertexCount(final Tessellator tessellator) {
		return TessellatorAccess.<Integer>read(tessellator, "vertexCount");
	}

	public static int normal(final Tessellator tessellator) {
		return TessellatorAccess.<Integer>read(tessellator, "normal");
	}

	public static boolean isDrawing(final Tessellator tessellator) {
		return TessellatorAccess.<Boolean>read(tessellator, "isDrawing");
	}

	public static boolean hasTexture(final Tessellator tessellator) {
		return TessellatorAccess.<Boolean>read(tessellator, "hasTexture");
	}

	public static boolean hasNormals(final Tessellator tessellator) {
		return TessellatorAccess.<Boolean>read(tessellator, "hasNormals");
	}

	private static <T> T read(final Tessellator tessellator, final String name) {
		try {
			final Field field = Tessellator.class.getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(tessellator);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

}