package dev.joid.base.opengl.capability;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import lombok.NonNull;

public class GlCapabilitiesTest {

	@Test
	public void readsACoreContext() {
		final GlCapabilities capabilities = GlCapabilities.read(new ContextBinding("3.3.0 NVIDIA 560.94", "3.30 NVIDIA via Cg compiler", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0, "GL_ARB_texture_border_clamp"));
		Assert.assertEquals(330, capabilities.getVersion());
		Assert.assertEquals(330, capabilities.getGlslVersion());
		Assert.assertSame(GlProfile.CORE, capabilities.getProfile());
		Assert.assertEquals("OpenGL 3.3 with GLSL 3.30 (Test renderer)", capabilities.getName());
		Assert.assertTrue(capabilities.hasExtension("GL_ARB_texture_border_clamp"));
		Assert.assertTrue(capabilities.hasVertexArrays() && capabilities.hasSamplerObjects() && capabilities.hasFrameBufferBlit());
		Assert.assertSame(GlFrameBufferFamily.CORE, capabilities.getFrameBufferFamily());
		Assert.assertEquals(16384, capabilities.getMaxTextureSize());
	}

	@Test
	public void readsAForwardCompatibleContext() {
		Assert.assertSame(GlProfile.FORWARD_COMPATIBLE_CORE, GlCapabilities.read(new ContextBinding("4.1 Metal - 88", "4.10", GlConstants.CONTEXT_CORE_PROFILE_BIT, GlConstants.CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT)).getProfile());
	}

	@Test
	public void readsOpenGl31WithoutTheCompatibilityExtensionAsCore() {
		Assert.assertSame(GlProfile.CORE, GlCapabilities.read(new ContextBinding("3.1 Mesa 24.0", "1.40", 0, 0)).getProfile());
		Assert.assertSame(GlProfile.COMPATIBILITY, GlCapabilities.read(new ContextBinding("3.1 Mesa 24.0", "1.40", 0, 0, "GL_ARB_compatibility")).getProfile());
	}

	@Test
	public void readsTheExtensionsOfALegacyContext() {
		final GlCapabilities capabilities = GlCapabilities.read(new ContextBinding("2.1 Mesa 24.0", "1.20", 0, 0, "GL_ARB_framebuffer_object", "GL_EXT_framebuffer_blit"));
		Assert.assertEquals(210, capabilities.getVersion());
		Assert.assertEquals(120, capabilities.getGlslVersion());
		Assert.assertSame(GlProfile.COMPATIBILITY, capabilities.getProfile());
		Assert.assertSame(GlFrameBufferFamily.CORE, capabilities.getFrameBufferFamily());
		Assert.assertFalse(capabilities.hasVertexArrays());
		Assert.assertFalse(capabilities.hasSamplerObjects());
	}

	@Test
	public void readsTheExtFrameBuffersWithOrWithoutBlit() {
		Assert.assertSame(GlFrameBufferFamily.EXT, GlCapabilities.read(new ContextBinding("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object")).getFrameBufferFamily());
		Assert.assertFalse(GlCapabilities.read(new ContextBinding("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object")).hasFrameBufferBlit());
		Assert.assertTrue(GlCapabilities.read(new ContextBinding("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object", "GL_EXT_framebuffer_blit")).hasFrameBufferBlit());
		Assert.assertNull(GlCapabilities.read(new ContextBinding("2.1", "1.20", 0, 0)).getFrameBufferFamily());
	}

	@Test
	public void refusesToReadWithoutAContext() {
		try {
			GlCapabilities.read(new ContextBinding(null, null, 0, 0));
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("No OpenGL context is current: create the context of JOID and make it current before registering the backend", e.getMessage());
		}
	}

	@Test
	public void choosesTheStrategiesOfACoreContext() {
		final GlStrategies strategies = GlStrategies.of(GlCapabilities.read(new ContextBinding("4.6.0 NVIDIA", "4.60 NVIDIA", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0)));
		Assert.assertSame(GlslDialect.GLSL_330, strategies.getDialect());
		Assert.assertSame(GlFrameBufferFamily.CORE, strategies.getFrameBufferFamily());
		Assert.assertTrue(strategies.isOwnVertexArray());
		Assert.assertTrue(strategies.isBlitMipmaps());
		Assert.assertSame(GlslDialect.GLSL_330, strategies.createTranslator().getDialect());
	}

	@Test
	public void choosesTheHighestDialectOfTheContext() {
		Assert.assertSame(GlslDialect.GLSL_110, GlCapabilitiesTest.dialect("2.0 Mesa", "1.10"));
		Assert.assertSame(GlslDialect.GLSL_120, GlCapabilitiesTest.dialect("2.1 Mesa", "1.20"));
		Assert.assertSame(GlslDialect.GLSL_130, GlCapabilitiesTest.dialect("3.0 Mesa", "1.30"));
		Assert.assertSame(GlslDialect.GLSL_140, GlCapabilitiesTest.dialect("3.1 Mesa", "1.40"));
		Assert.assertSame(GlslDialect.GLSL_150, GlCapabilitiesTest.dialect("3.2 Mesa", "1.50"));
		Assert.assertSame(GlslDialect.GLSL_330, GlCapabilitiesTest.dialect("4.5 Mesa", "4.50"));
	}

	@Test
	public void choosesTheStrategiesOfALegacyContext() {
		final GlStrategies strategies = GlStrategies.of(GlCapabilities.read(new ContextBinding("2.1 Metal", "1.20", 0, 0, "GL_EXT_framebuffer_object")));
		Assert.assertSame(GlslDialect.GLSL_120, strategies.getDialect());
		Assert.assertSame(GlFrameBufferFamily.EXT, strategies.getFrameBufferFamily());
		Assert.assertFalse(strategies.isOwnVertexArray());
		Assert.assertFalse(strategies.isBlitMipmaps());
	}

	@Test
	public void refusesAContextBelowOpenGl20() {
		try {
			GlStrategies.of(GlCapabilities.read(new ContextBinding("1.5 Mesa", "", 0, 0, "GL_EXT_framebuffer_object")));
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("JOID needs OpenGL 2.0 with GLSL 1.10, this context offers OpenGL 1.5 with GLSL 0.00 (Test renderer)", e.getMessage());
		}
	}

	@Test
	public void refusesAContextWithoutFrameBuffers() {
		try {
			GlStrategies.of(GlCapabilities.read(new ContextBinding("2.1 Mesa 24.0", "1.20", 0, 0)));
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("JOID needs framebuffer objects (OpenGL 3.0, GL_ARB_framebuffer_object or GL_EXT_framebuffer_object), this context offers OpenGL 2.1 with GLSL 1.20 (Test renderer)", e.getMessage());
		}
	}

	private static GlslDialect dialect(final String version, final String glsl) {
		return GlStrategies.of(GlCapabilities.read(new ContextBinding(version, glsl, 0, 0, "GL_ARB_framebuffer_object", "GL_ARB_compatibility"))).getDialect();
	}

	private static final class ContextBinding implements IGlBinding {

		private final Map<Integer, Integer> integerMap;
		private final Map<Integer, String>  stringMap;
		private final List<String>          extensionList;

		private ContextBinding(final String version, final String glsl, final int profileMask, final int flags, final String... extensions) {
			this.integerMap    = new HashMap<>();
			this.stringMap     = new HashMap<>();
			this.extensionList = Arrays.asList(extensions);
			this.stringMap.put(GlConstants.VERSION, version);
			this.stringMap.put(GlConstants.SHADING_LANGUAGE_VERSION, glsl);
			this.stringMap.put(GlConstants.RENDERER, "Test renderer");
			this.stringMap.put(GlConstants.EXTENSIONS, String.join(" ", extensions));
			this.integerMap.put(GlConstants.CONTEXT_PROFILE_MASK, profileMask);
			this.integerMap.put(GlConstants.CONTEXT_FLAGS, flags);
			this.integerMap.put(GlConstants.NUM_EXTENSIONS, extensions.length);
			this.integerMap.put(GlConstants.MAX_TEXTURE_SIZE, 16384);
		}

		@Override
		public void enable(final int capability) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void disable(final int capability) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEnabled(final int capability) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getInteger(final int name) {
			return this.integerMap.get(name);
		}

		@Override
		public String getString(final int name) {
			return this.stringMap.get(name);
		}

		@Override
		public String getString(final int name, final int index) {
			return this.extensionList.get(index);
		}

		@Override
		public void getFloats(final int name, final @NonNull float[] values) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void getIntegers(final int name, final @NonNull int[] values) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getVertexAttribi(final int index, final int name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getTexParameteri(final int target, final int name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getVertexAttribPointer(final int index, final int name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void getMaterialFloats(final int face, final int name, final @NonNull float[] values) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void getVertexAttribFloats(final int index, final int name, final @NonNull float[] values) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear(final int mask) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void readBuffer(final int buffer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels) {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IGlStateBinding getStateBinding() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IGlBufferBinding getBufferBinding() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IGlProgramBinding getProgramBinding() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IGlTextureBinding getTextureBinding() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family) {
			throw new UnsupportedOperationException();
		}

	}

}