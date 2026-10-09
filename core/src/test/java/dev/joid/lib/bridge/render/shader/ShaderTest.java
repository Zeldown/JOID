package dev.joid.lib.bridge.render.shader;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public class ShaderTest {

	private static final String VERTEX   = "uniform sampler2D u_Noise;\n\nvoid main() {\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "uniform sampler2D tex;\nuniform sampler2D u_Noise;\nuniform vec4 u_Tint;\nuniform float u_Weights[3];\n\nvoid main() {\n    fragColor = texture(tex, vec2(0.0)) * u_Tint * u_Weights[0];\n}\n";

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

	@Test
	public void writesAUniformIntoItsMember() {
		final TestShader shader = ShaderTest.create();
		shader.uniform("u_Tint", 1F, 0.5F, 0.25F, 1F).uniform("u_Weights", 1F, 2F);
		Assert.assertEquals(0.25F, shader.getBlock().getMember("u_Tint").getValues().getFloat(8), 0F);
		Assert.assertEquals(2F, shader.getBlock().getMember("u_Weights").getValues().getFloat(4), 0F);
		Assert.assertTrue(shader.getBlock().getMember("u_Tint").isDirty());
	}

	@Test
	public void refusesAnUndeclaredUniform() {
		try {
			ShaderTest.create().uniform("u_Radius", 1F);
			Assert.fail("An undeclared uniform must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The shader declares no uniform u_Radius", expected.getMessage());
		}
	}

	@Test
	public void refusesAnUndeclaredSampler() {
		try {
			ShaderTest.create().sampler("u_Mask", new RecordingTexture(), TextureFilter.LINEAR, TextureWrap.REPEAT);
			Assert.fail("An undeclared sampler must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The shader declares no sampler u_Mask", expected.getMessage());
		}
	}

	@Test
	public void numbersTheSamplersFromOneInTheOrderOfTheStages() {
		final TestShader shader = ShaderTest.create();
		Assert.assertEquals(1, shader.getSamplerMap().get("u_Noise").getUnit());
		Assert.assertEquals(2, shader.getSamplerMap().get("tex").getUnit());
	}

	@Test
	public void keepsTheTextureOfASampler() {
		final ITexture texture = new RecordingTexture();
		final TestShader shader = ShaderTest.create();
		shader.sampler("tex", texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);
		Assert.assertSame(texture, shader.getSamplerMap().get("tex").getTexture());
		Assert.assertSame(TextureFilter.LINEAR, shader.getSamplerMap().get("tex").getFilter());
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, shader.getSamplerMap().get("tex").getWrap());
	}

	@Test
	public void writesTheBuiltinsOfADraw() {
		final TestShader shader = ShaderTest.create();
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(4D, 0D, 0D);
		final RenderState state = new RenderState();
		state.setAlphaTest(true);
		state.setAlphaThreshold(0.5F);
		shader.builtins(state, new float[16], modelView);
		Assert.assertEquals(4F, shader.getBlock().getMember("uModelViewMatrix").getValues().getFloat(48), 0F);
		Assert.assertEquals(1, shader.getBlock().getMember(GlslShaderTranslator.ALPHA_TEST).getValues().getInt(0));
		Assert.assertEquals(0.5F, shader.getBlock().getMember(GlslShaderTranslator.ALPHA_THRESHOLD).getValues().getFloat(0), 0F);
	}

	@Test
	public void createsItsLineShaderOnce() {
		final TestShader shader = ShaderTest.create(new LineRenderBridge());
		final IShader line = shader.getLineShader();
		Assert.assertSame(line, shader.getLineShader());
		Assert.assertSame(line, line.getLineShader());
		Assert.assertTrue(((Shader) line).getVertex().isLine());
		Assert.assertSame(shader.getFragment(), ((Shader) line).getFragment());
		Assert.assertSame(BlendState.NORMAL, ((Shader) line).getBlend());
	}

	@Test
	public void givesItsLineShaderItsUniformsAndSamplers() {
		final ITexture texture = new RecordingTexture();
		final TestShader shader = ShaderTest.create(new LineRenderBridge());
		shader.uniform("u_Tint", 1F, 0.5F, 0.25F, 1F).sampler("tex", texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);
		final Shader line = (Shader) shader.getLineShader();
		Assert.assertEquals(0.25F, line.getBlock().getMember("u_Tint").getValues().getFloat(8), 0F);
		Assert.assertSame(texture, line.getSamplerMap().get("tex").getTexture());
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, line.getSamplerMap().get("tex").getWrap());

		shader.uniform("u_Tint", 0F, 0F, 0F, 1F);
		Assert.assertEquals(0F, ((Shader) shader.getLineShader()).getBlock().getMember("u_Tint").getValues().getFloat(8), 0F);
	}

	@Test
	public void writesTheLineWidthAndViewportOnlyIntoALineShader() {
		final TestShader shader = ShaderTest.create(new LineRenderBridge());
		final RenderState state = new RenderState();
		state.setLineWidth(3F);
		state.setViewportWidth(800);
		state.setViewportHeight(600);
		((Shader) shader.getLineShader()).builtins(state, new float[16], new MatrixStack());
		shader.builtins(state, new float[16], new MatrixStack());
		final UniformBlock block = ((Shader) shader.getLineShader()).getBlock();
		Assert.assertEquals(3F, block.getMember(GlslShaderTranslator.LINE_WIDTH).getValues().getFloat(0), 0F);
		Assert.assertEquals(600F, block.getMember(GlslShaderTranslator.LINE_VIEWPORT).getValues().getFloat(4), 0F);
		Assert.assertNull(shader.getBlock().getMember(GlslShaderTranslator.LINE_WIDTH));
	}

	private static TestShader create() {
		return ShaderTest.create(new RecordingRenderBridge());
	}

	private static TestShader create(final RenderBridge render) {
		return new TestShader(render, BlendState.NORMAL, ShaderSource.parse(ShaderStage.VERTEX, ShaderTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTest.FRAGMENT));
	}

	private static final class TestShader extends Shader {

		private TestShader(final RenderBridge render, final BlendState blend) {
			this(render, blend, ShaderSource.parse(ShaderStage.VERTEX, "void main() {}"), ShaderSource.parse(ShaderStage.FRAGMENT, "void main() {}"));
		}

		private TestShader(final RenderBridge render, final BlendState blend, final ShaderSource vertex, final ShaderSource fragment) {
			super(render, GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK), vertex, fragment, blend, true);
		}

	}

	private static final class LineRenderBridge extends RenderBridge {

		@Override
		public void clearDepth() {}

		@Override
		public void clearStencil() {}

		@Override
		public void clear(final float red, final float green, final float blue, final float alpha) {}

		@Override
		protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader shader) {}

		@Override
		public @NonNull ITexture createTexture() {
			return new RecordingTexture();
		}

		@Override
		public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
			return new TestShader(this, blend, vertex, fragment);
		}

	}

}