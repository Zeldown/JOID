package dev.joid.lib.bridge.render;

import java.lang.reflect.Field;

import dev.joid.demo.font.impl.bitmap.DemoPixelTextRenderer;
import dev.joid.lib.font.impl.glyph.GlyphTextRenderer;
import dev.joid.lib.font.impl.msdf.MsdfTextRenderer;
import dev.joid.lib.render.framebuffer.FrameBufferPool;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderSingletons {

	public static void reset() {
		RenderSingletons.clear(GlyphTextRenderer.class, MsdfTextRenderer.inst(), "shader");
		RenderSingletons.clear(GlyphTextRenderer.class, DemoPixelTextRenderer.inst(), "shader");
		FrameBufferPool.cleanup();
	}

	private static void clear(final Class<?> type, final Object instance, final String name) {
		try {
			final Field field = type.getDeclaredField(name);
			field.setAccessible(true);
			field.set(instance, null);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}