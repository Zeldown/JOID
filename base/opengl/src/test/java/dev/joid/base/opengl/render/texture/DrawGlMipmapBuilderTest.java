package dev.joid.base.opengl.render.texture;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.MipmapChain;

public class DrawGlMipmapBuilderTest {

	private final List<String> calls = new ArrayList<>();

	@Test
	public void drawsTheLevelsWithItsCompiledProgram() {
		final GlRenderBridge bridge = new GlRenderBridge(this.binding());
		Assert.assertTrue(bridge.getMipmapBuilder() instanceof DrawGlMipmapBuilder);

		bridge.getMipmapBuilder().build(bridge, GlTexture.create(bridge), MipmapChain.of(4, 4, true));
		Assert.assertTrue(this.calls.toString(), this.calls.contains("useProgram 7"));
		Assert.assertFalse(this.calls.toString(), this.calls.subList(0, this.calls.lastIndexOf("drawArrays")).contains("useProgram 0"));
	}

	private IGlBinding binding() {
		final IGlFrameBufferBinding frameBuffers = DrawGlMipmapBuilderTest.fake(IGlFrameBufferBinding.class, (method, args) -> method.getName().equals("genFramebuffer") ? 11 : null);
		return DrawGlMipmapBuilderTest.fake(IGlBinding.class, (method, args) -> {
			switch (method.getName()) {
			case "getString":
				return args.length > 1 ? null : args[0].equals(GlConstants.VERSION) ? "2.1" : args[0].equals(GlConstants.SHADING_LANGUAGE_VERSION) ? "1.20" : args[0].equals(GlConstants.EXTENSIONS) ? "GL_EXT_framebuffer_object" : "Fake";
			case "getInteger":
				return args[0].equals(GlConstants.MAX_TEXTURE_SIZE) ? 4096 : 0;
			case "getFrameBufferBinding":
				return frameBuffers;
			case "useProgram":
				this.calls.add("useProgram " + args[0]);
				return null;
			case "drawArrays":
				this.calls.add("drawArrays");
				return null;
			case "createProgram":
				return 7;
			case "createShader":
				return 3;
			case "getUniformLocation":
				return 1;
			case "getShaderi":
			case "getProgrami":
				return GlConstants.TRUE;
			case "genBuffer":
				return 5;
			case "genTexture":
				return 9;
			default:
				return null;
			}
		});
	}

	private static <T> T fake(final Class<T> type, final Answer answer) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
			final Object value = answer.answer(method, args == null ? new Object[0] : args);
			if (value != null || !method.getReturnType().isPrimitive()) {
				return value;
			}
			return method.getReturnType() == boolean.class ? (Object) false : method.getReturnType() == long.class ? (Object) 0L : method.getReturnType() == void.class ? null : (Object) 0;
		});
	}

	@FunctionalInterface
	private interface Answer {

		public Object answer(final Method method, final Object[] args);

	}

}