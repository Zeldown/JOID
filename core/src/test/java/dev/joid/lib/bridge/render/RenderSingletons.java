package dev.joid.lib.bridge.render;

import java.lang.reflect.Field;

import dev.joid.demo.font.impl.bitmap.DemoPixelTextRenderer;
import dev.joid.lib.draw.raster.ExternalRaster;
import dev.joid.lib.font.impl.glyph.GlyphTextRenderer;
import dev.joid.lib.font.impl.msdf.MsdfTextRenderer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderSingletons {

	public static void reset() {
		RenderSingletons.clear(GlyphTextRenderer.class, MsdfTextRenderer.inst(), "shader");
		RenderSingletons.clear(GlyphTextRenderer.class, DemoPixelTextRenderer.inst(), "shader");
		RenderSingletons.clear(ExternalRaster.class, ExternalRaster.inst(), "target");
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