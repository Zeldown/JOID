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
import dev.joid.lib.shader.impl.RoundedShader.RoundedShaderType;
import dev.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class RoundedNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) RoundedShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void roundsEveryCornerByDefault() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(6F);
		Assert.assertEquals(6F, effect.getRadius(), 0F);
		Assert.assertTrue(effect.isLeft());
		Assert.assertTrue(effect.isRight());
		Assert.assertTrue(effect.isTop());
		Assert.assertTrue(effect.isBottom());
		Assert.assertTrue(effect.isShaderEffect());
	}

	@Test
	public void roundsOnlyTheChosenSides() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(6F, true, false, false, true);
		Assert.assertTrue(effect.isLeft());
		Assert.assertFalse(effect.isRight());
		Assert.assertFalse(effect.isTop());
		Assert.assertTrue(effect.isBottom());
	}

	@Test
	public void readsItsSuppliedRadius() {
		final float[] radius = {2F};
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(() -> radius[0]);
		radius[0] = 9F;
		Assert.assertEquals(9F, effect.getRadius(), 0F);
		Assert.assertTrue(effect.isLeft() && effect.isRight() && effect.isTop() && effect.isBottom());
	}

	@Test
	public void readsItsSuppliedSides() {
		final boolean[] sides = {true};
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(() -> 4F, () -> sides[0], () -> !sides[0], () -> sides[0], () -> !sides[0]);
		Assert.assertTrue(effect.isLeft());
		Assert.assertFalse(effect.isRight());
		sides[0] = false;
		Assert.assertFalse(effect.isTop());
		Assert.assertTrue(effect.isBottom());
		Assert.assertEquals(4F, effect.getRadius(), 0F);
	}

	@Test
	public void replacesEachValue() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(6F);
		Assert.assertSame(effect, effect.radius(3F).left(false).right(false).top(false).bottom(false));
		Assert.assertEquals(3F, effect.getRadius(), 0F);
		Assert.assertFalse(effect.isLeft() || effect.isRight() || effect.isTop() || effect.isBottom());
	}

	@Test
	public void replacesEachValueWithASupplier() {
		final boolean[] side = {false};
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(6F);
		Assert.assertSame(effect, effect.radius(() -> 5F).left(() -> side[0]).right(() -> side[0]).top(() -> side[0]).bottom(() -> side[0]));
		side[0] = true;
		Assert.assertEquals(5F, effect.getRadius(), 0F);
		Assert.assertTrue(effect.isLeft() && effect.isRight() && effect.isTop() && effect.isBottom());
	}

	@Test
	public void bindsTheShaderAroundTheInnerBoxOfItsNode() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(5F, false, true, true, false);
		final RectNode node = RectNode.create(10D, 20D, 100D, 60D);
		effect.pre(node, 0D, 0D);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(5F, (Float) values.get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 25F, 105F, 80F}, (float[]) values.get("u_InnerRect"), 1E-3F);
		effect.post(node, 0D, 0D);
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void bindsTheShaderInsideEveryRoundedCorner() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(5F);
		effect.pre(RectNode.create(10D, 20D, 100D, 60D), 0D, 0D);
		Assert.assertArrayEquals(new float[] {15F, 25F, 105F, 75F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void roundsTheNodeThroughTheShaderPipeline() {
		final RoundedNodeEffect<Node> effect = RoundedNodeEffect.create(10F);
		final RectNode node = RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(effect);
		Assert.assertTrue(effect.toShaderPass(node) instanceof RoundedShaderPass);
		this.bridges.open(new NodeUI(node)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertEquals(100D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(300D, quad.getRight(), 1E-3D);
		Assert.assertEquals(150D, quad.getBottom(), 1E-3D);
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("u_Type"));
		Assert.assertArrayEquals(new float[] {110F, 110F, 290F, 140F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
		Assert.assertFalse(this.shader.isBound());
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