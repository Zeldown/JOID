package dev.joid.lib.bridge.render.shader;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;

public class ShaderTest {

	@Test
	public void bindsItselfWithItsBlend() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final TestShader shader = new TestShader(render, BlendState.PREMULTIPLIED);
		shader.bind();
		Assert.assertSame(shader, render.getShader());
		Assert.assertSame(BlendState.PREMULTIPLIED, render.getState().getBlend());
		Assert.assertTrue(shader.isBound());
	}

	@Test
	public void restoresThePreviousBlendOnUnbind() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		final TestShader shader = new TestShader(render, BlendState.PREMULTIPLIED);
		render.blend(BlendState.NORMAL);
		shader.bind();
		shader.unbind();
		Assert.assertNull(render.getShader());
		Assert.assertSame(BlendState.NORMAL, render.getState().getBlend());
		Assert.assertFalse(shader.isBound());
	}

	@Test
	public void keepsTheBlendOfAnUnbindWithoutBind() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		render.blend(BlendState.NORMAL);
		new TestShader(render, BlendState.PREMULTIPLIED).unbind();
		Assert.assertSame(BlendState.NORMAL, render.getState().getBlend());
	}

	@Test
	public void reportsWhetherItCompiled() {
		final RecordingRenderBridge render = new RecordingRenderBridge();
		Assert.assertTrue(new TestShader(render, BlendState.NORMAL).isActive());
	}

	private static final class TestShader extends Shader {

		private TestShader(final RecordingRenderBridge render, final BlendState blend) {
			super(render, blend, true, UniformBlock.create(Collections.emptyList(), ""), Collections.emptyList());
		}

	}

}