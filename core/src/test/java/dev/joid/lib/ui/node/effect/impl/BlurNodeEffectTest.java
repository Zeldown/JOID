package dev.joid.lib.ui.node.effect.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class BlurNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void blursThroughTheShaderPipeline() {
		Assert.assertTrue(BlurNodeEffect.create(4F).isShaderEffect());
	}

	@Test
	public void blursHorizontallyThenVertically() {
		final List<ShaderPass> passes = BlurNodeEffect.create(4F).toShaderPasses(RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertEquals(2, passes.size());
		Assert.assertEquals(150, passes.get(0).priority());
		Assert.assertEquals(151, passes.get(1).priority());
		Assert.assertEquals(4F, passes.get(0).expansion(), 0F);
		Assert.assertEquals(4F, passes.get(1).expansion(), 0F);
	}

	@Test
	public void replacesItsRadius() {
		final BlurNodeEffect<Node> effect = BlurNodeEffect.create(4F);
		Assert.assertSame(effect, effect.radius(9F));
		Assert.assertEquals(9F, effect.getRadiusSupplier().get(), 0F);
		Assert.assertEquals(9F, effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).get(0).expansion(), 0F);
	}

	@Test
	public void readsItsSuppliedRadiusForEachPass() {
		final float[] radius = {2F};
		final BlurNodeEffect<Node> effect = BlurNodeEffect.create(4F);
		Assert.assertSame(effect, effect.radius(() -> radius[0]));
		radius[0] = 7F;
		Assert.assertEquals(7F, effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).get(1).expansion(), 0F);
	}

	@Test
	public void turnsTheBlurOffWithoutRadius() {
		Assert.assertTrue(BlurNodeEffect.create(0F).toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).isEmpty());
		Assert.assertTrue(BlurNodeEffect.create(-2F).toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)).isEmpty());
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

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

}