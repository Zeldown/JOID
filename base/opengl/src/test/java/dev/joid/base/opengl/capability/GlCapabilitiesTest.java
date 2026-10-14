package dev.joid.base.opengl.capability;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;

public class GlCapabilitiesTest {

	@Test
	public void readsACoreContext() {
		final GlCapabilities capabilities = GlCapabilities.read(GlCapabilitiesTest.context("3.3.0 NVIDIA 560.94", "3.30 NVIDIA via Cg compiler", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0, "GL_ARB_texture_border_clamp"));
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
		Assert.assertSame(GlProfile.CORE_FORWARD_COMPATIBLE, GlCapabilities.read(GlCapabilitiesTest.context("4.1 Metal - 88", "4.10", GlConstants.CONTEXT_CORE_PROFILE_BIT, GlConstants.CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT)).getProfile());
	}

	@Test
	public void readsOpenGl31WithoutTheCompatibilityExtensionAsCore() {
		Assert.assertSame(GlProfile.CORE, GlCapabilities.read(GlCapabilitiesTest.context("3.1 Mesa 24.0", "1.40", 0, 0)).getProfile());
		Assert.assertSame(GlProfile.COMPATIBILITY, GlCapabilities.read(GlCapabilitiesTest.context("3.1 Mesa 24.0", "1.40", 0, 0, "GL_ARB_compatibility")).getProfile());
	}

	@Test
	public void readsTheExtensionsOfALegacyContext() {
		final GlCapabilities capabilities = GlCapabilities.read(GlCapabilitiesTest.context("2.1 Mesa 24.0", "1.20", 0, 0, "GL_ARB_framebuffer_object", "GL_EXT_framebuffer_blit"));
		Assert.assertEquals(210, capabilities.getVersion());
		Assert.assertEquals(120, capabilities.getGlslVersion());
		Assert.assertSame(GlProfile.COMPATIBILITY, capabilities.getProfile());
		Assert.assertSame(GlFrameBufferFamily.CORE, capabilities.getFrameBufferFamily());
		Assert.assertFalse(capabilities.hasVertexArrays());
		Assert.assertFalse(capabilities.hasSamplerObjects());
	}

	@Test
	public void readsTheExtFrameBuffersWithOrWithoutBlit() {
		Assert.assertSame(GlFrameBufferFamily.EXT, GlCapabilities.read(GlCapabilitiesTest.context("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object")).getFrameBufferFamily());
		Assert.assertFalse(GlCapabilities.read(GlCapabilitiesTest.context("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object")).hasFrameBufferBlit());
		Assert.assertTrue(GlCapabilities.read(GlCapabilitiesTest.context("2.1", "1.20", 0, 0, "GL_EXT_framebuffer_object", "GL_EXT_framebuffer_blit")).hasFrameBufferBlit());
		Assert.assertSame(GlFrameBufferFamily.UNSUPPORTED, GlCapabilities.read(GlCapabilitiesTest.context("2.1", "1.20", 0, 0)).getFrameBufferFamily());
	}

	@Test
	public void refusesToReadWithoutAContext() {
		try {
			GlCapabilities.read(GlCapabilitiesTest.context(null, null, 0, 0));
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("No OpenGL context is current: create the context of JOID and make it current before registering the backend", e.getMessage());
		}
	}

	@Test
	public void choosesTheStrategiesOfACoreContext() {
		final GlCapabilities capabilities = GlCapabilities.read(GlCapabilitiesTest.context("4.6.0 NVIDIA", "4.60 NVIDIA", GlConstants.CONTEXT_CORE_PROFILE_BIT, 0)).require();
		Assert.assertSame(GlslDialect.GLSL_330, capabilities.getDialect());
		Assert.assertSame(GlFrameBufferFamily.CORE, capabilities.getFrameBufferFamily());
		Assert.assertTrue(capabilities.hasVertexArrays());
		Assert.assertTrue(capabilities.hasFrameBufferBlit());
	}

	@Test
	public void choosesTheHighestDialectOfTheContext() {
		Assert.assertSame(GlslDialect.GLSL_120, GlCapabilitiesTest.dialect("2.1 Mesa", "1.20"));
		Assert.assertSame(GlslDialect.GLSL_130, GlCapabilitiesTest.dialect("3.0 Mesa", "1.30"));
		Assert.assertSame(GlslDialect.GLSL_140, GlCapabilitiesTest.dialect("3.1 Mesa", "1.40"));
		Assert.assertSame(GlslDialect.GLSL_150, GlCapabilitiesTest.dialect("3.2 Mesa", "1.50"));
		Assert.assertSame(GlslDialect.GLSL_330, GlCapabilitiesTest.dialect("4.5 Mesa", "4.50"));
	}

	@Test
	public void choosesTheStrategiesOfALegacyContext() {
		final GlCapabilities capabilities = GlCapabilities.read(GlCapabilitiesTest.context("2.1 Metal", "1.20", 0, 0, "GL_EXT_framebuffer_object")).require();
		Assert.assertSame(GlslDialect.GLSL_120, capabilities.getDialect());
		Assert.assertSame(GlFrameBufferFamily.EXT, capabilities.getFrameBufferFamily());
		Assert.assertFalse(capabilities.hasVertexArrays());
		Assert.assertFalse(capabilities.hasFrameBufferBlit());
	}

	@Test
	public void refusesAContextBelowOpenGl21() {
		for (final String[] context : new String[][] {{"1.5 Mesa", ""}, {"2.0 Mesa", "1.10"}, {"2.1 Mesa", "1.10"}}) {
			try {
				GlCapabilities.read(GlCapabilitiesTest.context(context[0], context[1], 0, 0, "GL_EXT_framebuffer_object")).require();
				Assert.fail();
			} catch (final IllegalStateException e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().startsWith("JOID needs OpenGL 2.1 with GLSL 1.20, this context offers OpenGL "));
				Assert.assertTrue(e.getMessage(), e.getMessage().endsWith(": update the graphics driver or create a newer OpenGL context"));
			}
		}
	}

	@Test
	public void refusesAContextWithoutFrameBuffers() {
		try {
			GlCapabilities.read(GlCapabilitiesTest.context("2.1 Mesa 24.0", "1.20", 0, 0)).require();
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("JOID needs framebuffer objects (OpenGL 3.0, GL_ARB_framebuffer_object or GL_EXT_framebuffer_object), this context offers OpenGL 2.1 with GLSL 1.20 (Test renderer): update the graphics driver or create a newer OpenGL context", e.getMessage());
		}
	}

	private static GlslDialect dialect(final String version, final String glsl) {
		return GlCapabilities.read(GlCapabilitiesTest.context(version, glsl, 0, 0, "GL_ARB_framebuffer_object", "GL_ARB_compatibility")).require().getDialect();
	}

	private static IGlBinding context(final String version, final String glsl, final int profileMask, final int flags, final String... extensions) {
		final Map<Integer, String> stringMap = new HashMap<>();
		final Map<Integer, Integer> integerMap = new HashMap<>();
		stringMap.put(GlConstants.VERSION, version);
		stringMap.put(GlConstants.SHADING_LANGUAGE_VERSION, glsl);
		stringMap.put(GlConstants.RENDERER, "Test renderer");
		stringMap.put(GlConstants.EXTENSIONS, String.join(" ", extensions));
		integerMap.put(GlConstants.CONTEXT_PROFILE_MASK, profileMask);
		integerMap.put(GlConstants.CONTEXT_FLAGS, flags);
		integerMap.put(GlConstants.NUM_EXTENSIONS, extensions.length);
		integerMap.put(GlConstants.MAX_TEXTURE_SIZE, 16384);
		return (IGlBinding) Proxy.newProxyInstance(IGlBinding.class.getClassLoader(), new Class<?>[] {IGlBinding.class}, (proxy, method, args) -> {
			if ("getInteger".equals(method.getName())) {
				return integerMap.get(args[0]);
			}
			if ("getString".equals(method.getName())) {
				return args.length == 2 ? Arrays.asList(extensions).get((int) args[1]) : stringMap.get(args[0]);
			}
			throw new UnsupportedOperationException();
		});
	}

}