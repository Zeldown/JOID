package dev.joid.lib.ui.node.effect.impl;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.ShadowShader;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class ShadowNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) ShadowShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void glowsAroundItsNodeByDefault() {
		final ShadowNodeEffect<Node> effect = ShadowNodeEffect.create(Color.RED, 8F);
		Assert.assertSame(Color.RED, effect.getColor());
		Assert.assertEquals(8F, effect.getBlur(), 0F);
		Assert.assertEquals(0D, effect.getOffsetX(), 0D);
		Assert.assertEquals(0D, effect.getOffsetY(), 0D);
		Assert.assertFalse(effect.isShaderEffect());
	}

	@Test
	public void dropsTheShadowByItsOffset() {
		final ShadowNodeEffect<Node> effect = ShadowNodeEffect.create(Color.BLACK, 12F, 2D, 6D);
		Assert.assertEquals(12F, effect.getBlur(), 0F);
		Assert.assertEquals(2D, effect.getOffsetX(), 0D);
		Assert.assertEquals(6D, effect.getOffsetY(), 0D);
	}

	@Test
	public void replacesEachValue() {
		final ShadowNodeEffect<Node> effect = ShadowNodeEffect.create(Color.RED, 8F);
		Assert.assertSame(effect, effect.blur(4F).color(Color.BLUE).offset(1D, 3D));
		Assert.assertSame(Color.BLUE, effect.getColor());
		Assert.assertEquals(4F, effect.getBlur(), 0F);
		Assert.assertEquals(1D, effect.getOffsetX(), 0D);
		Assert.assertEquals(3D, effect.getOffsetY(), 0D);
	}

	@Test
	public void readsItsSuppliedValues() {
		final double[] offset = {1D};
		final ShadowNodeEffect<Node> effect = ShadowNodeEffect.create(Color.RED, 8F);
		Assert.assertSame(effect, effect.blur(() -> (float) offset[0] * 10F).color(() -> Color.GREEN).offset(() -> offset[0], () -> -offset[0]));
		offset[0] = 2D;
		Assert.assertEquals(20F, effect.getBlur(), 0F);
		Assert.assertSame(Color.GREEN, effect.getColor());
		Assert.assertEquals(2D, effect.getOffsetX(), 0D);
		Assert.assertEquals(-2D, effect.getOffsetY(), 0D);
	}

	@Test
	public void drawsTheShadowUnderItsNode() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(ShadowNodeEffect.create(new Color(0.1F, 0.3F, 0.5F, 0.5F), 8F, 0D, 4D)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw shadow = this.bridges.getRender().getDraws(0.1F, 0.3F, 0.5F).get(0);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(draws.indexOf(shadow) < draws.indexOf(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0)));
		Assert.assertSame(this.shader, shadow.getShader());
		Assert.assertEquals(88D, shadow.getLeft(), 1E-3D);
		Assert.assertEquals(92D, shadow.getTop(), 1E-3D);
		Assert.assertEquals(312D, shadow.getRight(), 1E-3D);
		Assert.assertEquals(166D, shadow.getBottom(), 1E-3D);
		Assert.assertEquals(0F, (Float) values.get("u_Radius"), 0F);
		Assert.assertEquals(8F, (Float) values.get("u_Blur"), 0F);
		Assert.assertArrayEquals(new float[] {100F, 104F, 300F, 154F}, (float[]) values.get("u_Box"), 1E-3F);
	}

	@Test
	public void followsTheRoundedCornersOfItsNode() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(Color.WHITE).effect(RoundedNodeEffect.create(12F)).effect(ShadowNodeEffect.create(Color.BLACK, 8F)))).frame();
		Assert.assertEquals(12F, (Float) this.shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {100F, 100F, 300F, 150F}, (float[]) this.shader.getValues().get("u_Box"), 1E-3F);
	}

	@Test
	public void followsTheCircleOfItsNode() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 100D).color(Color.WHITE).effect(CircleNodeEffect.create()).effect(ShadowNodeEffect.create(Color.BLACK, 8F)))).frame();
		Assert.assertEquals(50F, (Float) this.shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {150F, 100F, 250F, 200F}, (float[]) this.shader.getValues().get("u_Box"), 1E-3F);
	}

	@Test
	public void followsACircleNode() {
		this.bridges.open(new NodeUI(CircleNode.create(100D, 100D, 60D).color(Color.WHITE).effect(ShadowNodeEffect.create(Color.BLACK, 8F, 5D, 5D)))).frame();
		Assert.assertEquals(30F, (Float) this.shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {105F, 105F, 165F, 165F}, (float[]) this.shader.getValues().get("u_Box"), 1E-3F);
	}

	@Test
	public void drawsASharpShadowWithoutBlur() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(Color.WHITE).effect(ShadowNodeEffect.create(new Color(0.1F, 0.3F, 0.5F, 0.5F), 0F, 10D, 10D)))).frame();
		final Draw shadow = this.bridges.getRender().getDraws(0.1F, 0.3F, 0.5F).get(0);
		Assert.assertSame(RoundedShader.inst().getShader(), shadow.getShader());
		Assert.assertEquals(110D, shadow.getLeft(), 1E-3D);
		Assert.assertEquals(110D, shadow.getTop(), 1E-3D);
		Assert.assertEquals(310D, shadow.getRight(), 1E-3D);
		Assert.assertEquals(160D, shadow.getBottom(), 1E-3D);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingColor() {
		ShadowNodeEffect.create(null, 8F);
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