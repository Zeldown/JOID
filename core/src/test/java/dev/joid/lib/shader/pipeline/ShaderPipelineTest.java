package dev.joid.lib.shader.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.bridge.clock.SystemClockBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

import lombok.Getter;
import lombok.NonNull;

public class ShaderPipelineTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	private final List<String> log = new ArrayList<>();

	@Before
	public void emptyThePool() {
		ShaderPipeline.cleanup();
	}

	@After
	public void releaseThePool() {
		ShaderPipeline.cleanup();
		BridgeHandler.CLOCK.register(new SystemClockBridge());
	}

	@Test
	public void drawsDirectlyWithoutPasses() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox);
		Assert.assertEquals(1, this.render.getCaptures().size());
		Assert.assertNull(this.render.getLast().getState().getFrameBuffer());
		Assert.assertTrue(this.render.getFrameBuffers().isEmpty());
	}

	@Test
	public void drawsASinglePassThroughAFrameBuffer() {
		final RecordingPass pass = new RecordingPass("mask", 100, 0F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, pass);
		Assert.assertEquals(Arrays.asList("mask texture", "mask unbind"), this.log);
		Assert.assertEquals(2, this.render.getFrameBuffers().size());
		Assert.assertEquals(100, this.render.getFrameBuffers().get(0).getWidth());
		Assert.assertEquals(50, this.render.getFrameBuffers().get(0).getHeight());
		Assert.assertEquals(2, this.render.getCaptures().size());
	}

	@Test
	public void paintsTheContentIntoTheFirstFrameBuffer() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		final Capture content = this.render.getCaptures().get(0);
		Assert.assertSame(this.render.getFrameBuffers().get(0), content.getState().getFrameBuffer());
		Assert.assertEquals(100, content.getState().getViewportWidth());
		Assert.assertEquals(50, content.getState().getViewportHeight());
		Assert.assertSame(BlendState.NORMAL, content.getState().getBlend());
		Assert.assertNull(content.getState().getShader());
		Assert.assertEquals(10D, content.getLeft(), 1E-3D);
		Assert.assertEquals(70D, content.getBottom(), 1E-3D);
	}

	@Test
	public void compositesTheLastPassOnTheScreen() {
		final RecordingPass pass = new RecordingPass("mask", 100, 0F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, pass);
		final Capture composite = this.render.getLast();
		Assert.assertNull(composite.getState().getFrameBuffer());
		Assert.assertSame(this.render.getFrameBuffers().get(0).getTexture(), composite.getState().getTexture());
		Assert.assertSame(TextureFilter.LINEAR, composite.getState().getTextureFilter());
		Assert.assertSame(BlendState.PREMULTIPLIED, composite.getState().getBlend());
		Assert.assertSame(pass.getShader(), composite.getState().getShader());
		Assert.assertEquals(1F, composite.getState().getRed(), 0F);
		Assert.assertEquals(1F, composite.getState().getAlpha(), 0F);
		Assert.assertEquals(10D, composite.getLeft(), 1E-3D);
		Assert.assertEquals(110D, composite.getRight(), 1E-3D);
		Assert.assertEquals(20D, composite.getTop(), 1E-3D);
		Assert.assertEquals(70D, composite.getBottom(), 1E-3D);
		Assert.assertEquals(70F, composite.getY(0), 1E-3F);
		Assert.assertEquals(0F, composite.getV(0), 0F);
		Assert.assertEquals(1F, composite.getV(2), 0F);
		Assert.assertSame(TextureWrap.CLAMP_TO_BORDER, composite.getState().getTextureWrap());
	}

	@Test
	public void spreadsARotatedCompositeByOneTexelToSmoothItsEdges() {
		this.render.getModelView().push();
		try {
			this.render.getModelView().rotate(30D, 0D, 0D, 1D);
			ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		} finally {
			this.render.getModelView().pop();
		}

		final Capture composite = this.render.getLast();
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, composite.getState().getTextureWrap());
		Assert.assertEquals(9D, composite.getLeft(), 1E-3D);
		Assert.assertEquals(111D, composite.getRight(), 1E-3D);
		Assert.assertEquals(19D, composite.getTop(), 1E-3D);
		Assert.assertEquals(71D, composite.getBottom(), 1E-3D);
		Assert.assertEquals(-0.01F, composite.getU(0), 1E-6F);
		Assert.assertEquals(-0.02F, composite.getV(0), 1E-6F);
	}

	@Test
	public void restoresTheRenderStateAfterTheFrameBuffers() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertNull(this.render.getState().getFrameBuffer());
		Assert.assertNull(this.render.getState().getShader());
		Assert.assertNull(this.render.getState().getTexture());
		Assert.assertEquals(1920, this.render.getState().getViewportWidth());
		Assert.assertEquals(1080, this.render.getState().getViewportHeight());
		Assert.assertSame(BlendState.DISABLED, this.render.getState().getBlend());
		Assert.assertEquals(1D, this.render.getPixelGrid().getScaleX(), 1E-6D);
		ShaderPipelineTest.assertNoPushedState(this.render);
	}

	@Test
	public void chainsThePassesByPriority() {
		final RecordingPass border = new RecordingPass("border", 200, 0F, this.log);
		final RecordingPass rounded = new RecordingPass("rounded", 100, 0F, this.log);
		final RecordingPass blur = new RecordingPass("blur", 150, 0F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, border, rounded, blur);
		Assert.assertEquals(Arrays.asList("rounded texture", "rounded unbind", "blur texture", "blur unbind", "border texture", "border unbind"), this.log);

		final List<IFrameBuffer> frameBuffers = this.render.getFrameBuffers();
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(4, captures.size());
		Assert.assertSame(frameBuffers.get(1), captures.get(1).getState().getFrameBuffer());
		Assert.assertSame(frameBuffers.get(0).getTexture(), captures.get(1).getState().getTexture());
		Assert.assertSame(rounded.getShader(), captures.get(1).getState().getShader());
		Assert.assertSame(frameBuffers.get(0), captures.get(2).getState().getFrameBuffer());
		Assert.assertSame(frameBuffers.get(1).getTexture(), captures.get(2).getState().getTexture());
		Assert.assertNull(captures.get(3).getState().getFrameBuffer());
		Assert.assertSame(frameBuffers.get(0).getTexture(), captures.get(3).getState().getTexture());
		Assert.assertSame(border.getShader(), captures.get(3).getState().getShader());
	}

	@Test
	public void growsTheFrameBufferByTheLargestExpansion() {
		final RecordingPass blur = new RecordingPass("blur", 150, 4F, this.log);
		final RecordingPass border = new RecordingPass("border", 200, 10F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, blur, border);
		Assert.assertEquals(10D, border.getContext().getExpansion(), 0D);
		Assert.assertSame(blur.getContext(), border.getContext());
		Assert.assertEquals(120, this.render.getFrameBuffers().get(0).getWidth());
		Assert.assertEquals(70, this.render.getFrameBuffers().get(0).getHeight());
		Assert.assertEquals(0D, this.render.getLast().getLeft(), 1E-3D);
		Assert.assertEquals(80D, this.render.getLast().getBottom(), 1E-3D);
	}

	@Test
	public void drawsAnExpandedSinglePassThroughAFrameBuffer() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("blur", 150, 4F, this.log));
		Assert.assertEquals(Arrays.asList("blur texture", "blur unbind"), this.log);
		Assert.assertEquals(2, this.render.getFrameBuffers().size());
	}

	@Test
	public void sizesTheFrameBufferInWindowPixels() {
		this.render.resize(1366, 768);
		this.render.getProjection().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final RecordingPass pass = new RecordingPass("mask", 100, 0F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, pass);
		Assert.assertEquals(72, this.render.getFrameBuffers().get(0).getWidth());
		Assert.assertEquals(36, this.render.getFrameBuffers().get(0).getHeight());
		Assert.assertEquals(72, pass.getContext().getTextureWidth());
		Assert.assertEquals(72, this.render.getCaptures().get(0).getState().getViewportWidth());
	}

	@Test
	public void drawsAnEmptyAreaWithoutFrameBuffer() {
		ShaderPipeline.render(10D, 20D, 0D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		ShaderPipeline.render(10D, 20D, 100D, -5D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertTrue(this.log.isEmpty());
		Assert.assertTrue(this.render.getFrameBuffers().isEmpty());
		Assert.assertEquals(2, this.render.getCaptures().size());
	}

	@Test
	public void reusesItsFrameBuffersForTheSameSize() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		ShaderPipeline.render(300D, 400D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertEquals(2, this.render.getFrameBuffers().size());
		ShaderPipeline.render(10D, 20D, 60D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertEquals(4, this.render.getFrameBuffers().size());
	}

	@Test
	public void givesANestedPipelineItsOwnFrameBuffers() {
		final RecordingPass inner = new RecordingPass("inner", 100, 0F, this.log);
		ShaderPipeline.render(10D, 20D, 100D, 50D, () -> ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, inner), new RecordingPass("outer", 100, 0F, this.log));
		Assert.assertEquals(Arrays.asList("inner texture", "inner unbind", "outer texture", "outer unbind"), this.log);
		Assert.assertEquals(4, this.render.getFrameBuffers().size());
		Assert.assertSame(this.render.getFrameBuffers().get(2), this.render.getCaptures().get(0).getState().getFrameBuffer());
		Assert.assertSame(this.render.getFrameBuffers().get(0), this.render.getCaptures().get(1).getState().getFrameBuffer());
	}

	@Test
	public void deletesThePooledFrameBuffers() {
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		ShaderPipeline.cleanup();
		for (final IFrameBuffer frameBuffer : this.render.getFrameBuffers()) {
			Assert.assertTrue(((RecordingFrameBuffer) frameBuffer).isDeleted());
		}

		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertEquals(4, this.render.getFrameBuffers().size());
		Assert.assertFalse(((RecordingFrameBuffer) this.render.getFrameBuffers().get(3)).isDeleted());
	}

	@Test
	public void releasesTheFrameBuffersLeftUnusedForFiveSeconds() {
		final ManualClockBridge clock = ManualClockBridge.create(1000L);
		BridgeHandler.CLOCK.register(clock);
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		ShaderPipeline.render(10D, 20D, 60D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		clock.advance(3000L);
		ShaderPipeline.render(10D, 20D, 60D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		clock.advance(1999L);
		ShaderPipeline.releaseUnused();
		Assert.assertFalse(((RecordingFrameBuffer) this.render.getFrameBuffers().get(0)).isDeleted());
		clock.advance(1L);
		ShaderPipeline.releaseUnused();
		Assert.assertTrue(((RecordingFrameBuffer) this.render.getFrameBuffers().get(0)).isDeleted());
		Assert.assertTrue(((RecordingFrameBuffer) this.render.getFrameBuffers().get(1)).isDeleted());
		Assert.assertFalse(((RecordingFrameBuffer) this.render.getFrameBuffers().get(2)).isDeleted());
		Assert.assertFalse(((RecordingFrameBuffer) this.render.getFrameBuffers().get(3)).isDeleted());
		ShaderPipeline.render(10D, 20D, 100D, 50D, this::drawBox, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertEquals(6, this.render.getFrameBuffers().size());
	}

	@Test
	public void keepsApartTheSizesThatShareAPackedKey() {
		ShaderPipeline.render(0D, 0D, 1D, 65537D, () -> {}, new RecordingPass("mask", 100, 0F, this.log));
		ShaderPipeline.render(0D, 0D, 2D, 1D, () -> {}, new RecordingPass("mask", 100, 0F, this.log));
		Assert.assertEquals(4, this.render.getFrameBuffers().size());
		Assert.assertEquals(2, this.render.getFrameBuffers().get(2).getWidth());
		Assert.assertEquals(1, this.render.getFrameBuffers().get(2).getHeight());
	}

	@Test
	public void rendersANodeWithItsBounds() {
		final RectNode node = RectNode.create(10D, 20D, 100D, 50D);
		final RecordingPass array = new RecordingPass("array", 100, 0F, this.log);
		final RecordingPass texture = new RecordingPass("texture", 100, 0F, this.log);
		ShaderPipeline.render(node, this::drawBox, array);
		ShaderPipeline.render(node, new ArrayList<>(Arrays.asList(texture)), this::drawBox);
		Assert.assertEquals(10D, array.getContext().getX(), 0D);
		Assert.assertEquals(20D, array.getContext().getY(), 0D);
		Assert.assertEquals(100D, texture.getContext().getWidth(), 0D);
		Assert.assertEquals(50D, texture.getContext().getHeight(), 0D);
	}

	@Test
	public void drawsANodeWithoutPassesDirectly() {
		ShaderPipeline.render(RectNode.create(10D, 20D, 100D, 50D), new ArrayList<>(), this::drawBox);
		Assert.assertEquals(1, this.render.getCaptures().size());
		Assert.assertTrue(this.render.getFrameBuffers().isEmpty());
	}

	@Test
	public void restoresTheRenderStateWhenTheDrawFails() {
		try {
			ShaderPipeline.render(10D, 20D, 100D, 50D, () -> {
				throw new IllegalStateException("draw");
			}, new RecordingPass("rounded", 100, 0F, this.log));
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("draw", exception.getMessage());
		}
		Assert.assertTrue(this.log.isEmpty());
		Assert.assertNull(this.render.getState().getFrameBuffer());
		ShaderPipelineTest.assertNoPushedState(this.render);
	}

	@Test
	public void rendersAReadOnlyListOfPasses() {
		final List<IShaderPass> passes = Collections.unmodifiableList(Arrays.asList(new RecordingPass("border", 200, 0F, this.log), new RecordingPass("rounded", 100, 0F, this.log)));
		ShaderPipeline.render(10D, 20D, 100D, 50D, passes, () -> {});
		Assert.assertEquals(Arrays.asList("rounded texture", "rounded unbind", "border texture", "border unbind"), this.log);
	}

	private void drawBox() {
		DrawUtils.SHAPE.drawRect(10D, 20D, 100D, 50D, new Color(0.2F, 0.4F, 0.6F, 1F));
	}

	private static void assertNoPushedState(final CapturingRenderBridge render) {
		try {
			render.popState();
			Assert.fail("Every pushed state must be popped");
		} catch (final NoSuchElementException expected) {
			Assert.assertNotNull(expected);
		}
	}

	@Getter
	private static final class RecordingPass implements IShaderPass {

		private final String          name;
		private final int             priority;
		private final float           expansion;
		private final List<String>    log;
		private final RecordingShader shader = new RecordingShader();

		private ShaderPassContext context;

		private RecordingPass(final String name, final int priority, final float expansion, final List<String> log) {
			this.name = name;
			this.priority = priority;
			this.expansion = expansion;
			this.log = log;
		}

		@Override
		public void unbind() {
			this.log.add(this.name + " unbind");
			this.shader.unbind();
		}

		@Override
		public int priority() {
			return this.priority;
		}

		@Override
		public float expansion() {
			return this.expansion;
		}

		@Override
		public void bind(final @NonNull ShaderPassContext context) {
			this.context = context;
			this.log.add(this.name + " texture");
			this.shader.bind();
		}

	}

}