package dev.joid.lib.ui.node.effect.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.CircleShader.RoundedShaderType;
import dev.joid.lib.shader.pipeline.pass.CircleShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class CircleNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) CircleShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void cutsTheNodeThroughTheShaderPipeline() {
		final CircleNodeEffect<Node> effect = CircleNodeEffect.create();
		Assert.assertTrue(effect.isShaderEffect());
		Assert.assertTrue(effect.toShaderPass(RectNode.create(0D, 0D, 10D, 10D)) instanceof CircleShaderPass);
	}

	@Test
	public void leavesTheShaderToItsPass() {
		final CircleNodeEffect<Node> effect = CircleNodeEffect.create();
		final RectNode node = RectNode.create(10D, 20D, 100D, 60D);
		effect.pre(node, 0D, 0D);
		effect.post(node, 0D, 0D);
		Assert.assertTrue(this.shader.getValues().isEmpty());
	}

	@Test
	public void cutsTheRenderedNodeIntoACircle() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 80D, 40D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(CircleNodeEffect.create()))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertEquals(100D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(180D, quad.getRight(), 1E-3D);
		Assert.assertEquals(20F, (Float) this.shader.getValues().get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {140F, 120F}, (float[]) this.shader.getValues().get("center"), 0F);
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("type"));
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