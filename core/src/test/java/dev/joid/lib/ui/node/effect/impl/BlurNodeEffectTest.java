package dev.joid.lib.ui.node.effect.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.color.Color;
import dev.joid.lib.render.framebuffer.FrameBufferPool;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class BlurNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void blursHorizontallyThenVertically() {
		final List<IShaderPass> passes = BlurNodeEffect.create(4F).toShaderPasses(RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertEquals(2, passes.size());
		Assert.assertEquals(150, BlurNodeEffect.create(4F).getPriority());
		Assert.assertEquals(4F, passes.get(0).expansion(), 0F);
		Assert.assertEquals(4F, passes.get(1).expansion(), 0F);
		Assert.assertEquals(33D, passes.get(0).reach(1D), 0D);
		Assert.assertEquals(81D, passes.get(1).reach(20D), 0D);
	}

	@Test
	public void replacesItsRadius() {
		final BlurNodeEffect effect = BlurNodeEffect.create(4F);
		Assert.assertSame(effect, effect.radius(9F));
		Assert.assertEquals(9F, effect.getRadius(), 0F);
		Assert.assertEquals(9F, effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).get(0).expansion(), 0F);
	}

	@Test
	public void readsItsSuppliedRadiusForEachPass() {
		final float[] radius = {2F};
		final BlurNodeEffect effect = BlurNodeEffect.create(4F);
		Assert.assertSame(effect, effect.radius(() -> radius[0]));
		radius[0] = 7F;
		Assert.assertEquals(7F, effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).get(1).expansion(), 0F);
	}

	@Test
	public void startsFromASuppliedRadius() {
		final float[] radius = {2F};
		final BlurNodeEffect effect = BlurNodeEffect.create(() -> radius[0]);
		radius[0] = 6F;
		Assert.assertEquals(6F, effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).get(0).expansion(), 0F);
	}

	@Test
	public void turnsTheBlurOffWithoutRadius() {
		Assert.assertFalse(BlurNodeEffect.create(0F).shouldApply(RectNode.create(0D, 0D, 10D, 10D)));
		Assert.assertFalse(BlurNodeEffect.create(-2F).shouldApply(RectNode.create(0D, 0D, 10D, 10D)));
		Assert.assertTrue(BlurNodeEffect.create(2F).shouldApply(RectNode.create(0D, 0D, 10D, 10D)));
	}

	@Test
	public void drawsTheNodeAsItIsWithoutRadius() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(BlurNodeEffect.create(0F)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertEquals(0.2F, quad.getRed(), 0F);
		Assert.assertEquals(100D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(100D, quad.getTop(), 1E-3D);
		Assert.assertEquals(300D, quad.getRight(), 1E-3D);
		Assert.assertEquals(150D, quad.getBottom(), 1E-3D);
	}

	@Test
	public void spreadsTheBlurredNodeAroundItsBox() {
		final RecordingShader shader = (RecordingShader) BlurShader.inst().getShader();
		shader.getValues().clear();
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(BlurNodeEffect.create(4F)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size());
		Assert.assertEquals(96D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(96D, quad.getTop(), 1E-3D);
		Assert.assertEquals(304D, quad.getRight(), 1E-3D);
		Assert.assertEquals(154D, quad.getBottom(), 1E-3D);
		Assert.assertEquals(4F, (Float) shader.getValues().get("u_Radius"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) shader.getValues().get("u_Direction"), 0F);
	}

	@Test
	public void keepsTheFrameBuffersOfAZoomedNodeToTheView() throws ReflectiveOperationException {
		FrameBufferPool.cleanup();
		final RectNode node = RectNode.create(0D, 0D, 1920D, 1080D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
		node.effect(ShadowNodeEffect.create(Color.BLACK, 12F)).effect(BlurNodeEffect.create(4F)).effect(TransformNodeEffect.create(Transformation.create().scale(Scale.create(6D, 6D, 1D))));
		this.bridges.open(new NodeUI(node)).frame();

		int width = 0;
		int height = 0;
		final Field pool = FrameBufferPool.class.getDeclaredField("POOL");
		pool.setAccessible(true);
		for (final Object pooled : ((Map<?, ?>) pool.get(null)).values()) {
			final Field frameBuffers = pooled.getClass().getDeclaredField("frameBuffers");
			frameBuffers.setAccessible(true);
			for (final IFrameBuffer frameBuffer : (IFrameBuffer[]) frameBuffers.get(pooled)) {
				width = Math.max(width, frameBuffer.getWidth());
				height = Math.max(height, frameBuffer.getHeight());
			}
		}
		FrameBufferPool.cleanup();
		Assert.assertEquals(4096, width);
		Assert.assertEquals(2048, height);
	}

}