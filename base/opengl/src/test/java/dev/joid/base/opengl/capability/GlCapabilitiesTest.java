package dev.joid.base.opengl.capability;

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
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.NonNull;

public class GlCapabilitiesTest {

	@Test
	public void readsACoreContext() {
		final GlCapabilities capabilities = GlCapabilities.read(new ContextBinding("3.3.0 NVIDIA 560.94", "3.30 NVIDIA via Cg compiler", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0, "GL_ARB_texture_border_clamp"));
		Assert.assertEquals(330, capabilities.getVersion());
		Assert.assertEquals(330, capabilities.getGlslVersion());
		Assert.assertSame(GlProfile.CORE, capabilities.getProfile());
		Assert.assertEquals("OpenGL 3.3", capabilities.getName());
		Assert.assertTrue(capabilities.hasExtension("GL_ARB_texture_border_clamp"));
		Assert.assertTrue(capabilities.hasVertexArrays() && capabilities.hasUniformBuffers() && capabilities.hasSamplerObjects() && capabilities.hasFrameBufferObjects());
		Assert.assertEquals(16384, capabilities.getMaxTextureSize());
		Assert.assertArrayEquals(new float[] {1F, 10F}, capabilities.getAliasedLineWidthRange(), 0F);
		Assert.assertArrayEquals(new float[] {0.5F, 8F}, capabilities.getSmoothLineWidthRange(), 0F);
	}

	@Test
	public void readsAForwardCompatibleContext() {
		Assert.assertSame(GlProfile.FORWARD_COMPATIBLE_CORE, GlCapabilities.read(new ContextBinding("4.1 Metal - 88", "4.10", GlConstants.CONTEXT_CORE_PROFILE_BIT, GlConstants.CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT)).getProfile());
	}

	@Test
	public void readsTheExtensionsOfALegacyContext() {
		final GlCapabilities capabilities = GlCapabilities.read(new ContextBinding("2.1 Mesa 24.0", "1.20", 0, 0, "GL_ARB_framebuffer_object", "GL_EXT_framebuffer_blit"));
		Assert.assertEquals(210, capabilities.getVersion());
		Assert.assertEquals(120, capabilities.getGlslVersion());
		Assert.assertSame(GlProfile.COMPATIBILITY, capabilities.getProfile());
		Assert.assertTrue(capabilities.hasFrameBufferObjects());
		Assert.assertFalse(capabilities.hasVertexArrays());
	}

	@Test
	public void choosesTheCurrentStrategiesOnOpenGl33() {
		final GlStrategies strategies = GlStrategies.of(GlCapabilities.read(new ContextBinding("4.6.0 NVIDIA", "4.60 NVIDIA", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0)));
		Assert.assertSame(GlslDialect.GLSL_330, strategies.getDialect());
		Assert.assertSame(UniformLayout.BLOCK, strategies.getUniformLayout());
		Assert.assertSame(GlslDialect.GLSL_330, strategies.createTranslator().getDialect());
	}

	@Test
	public void refusesAContextBelowOpenGl33() {
		try {
			GlStrategies.of(GlCapabilities.read(new ContextBinding("2.1 Mesa 24.0", "1.20", 0, 0)));
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("JOID needs OpenGL 3.3 with GLSL 3.30, this context offers OpenGL 2.1 with GLSL 1.20 (Test renderer)", e.getMessage());
		}
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
			values[0] = name == GlConstants.ALIASED_LINE_WIDTH_RANGE ? 1F : 0.5F;
			values[1] = name == GlConstants.ALIASED_LINE_WIDTH_RANGE ? 10F : 8F;
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
		public @NonNull IGlFrameBufferBinding getFrameBufferBinding() {
			throw new UnsupportedOperationException();
		}

	}

}