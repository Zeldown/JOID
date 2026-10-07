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
import dev.joid.lib.shader.impl.BorderShader;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class BorderNodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) BorderShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void drawsAFilledOuterBorderByDefault() {
		final BorderNodeEffect effect = BorderNodeEffect.create(new Color(0.2F, 0.4F, 0.6F, 1F), 3F);
		Assert.assertTrue(effect.isShaderEffect());
		Assert.assertTrue(effect.isFill());
		Assert.assertSame(BorderMode.OUT, effect.getMode());
		Assert.assertEquals(3F, effect.getWidthSupplier().get(), 0F);
	}

	@Test
	public void drawsTheChosenSide() {
		Assert.assertSame(BorderMode.IN, BorderNodeEffect.create(new Color(0.2F, 0.4F, 0.6F, 1F), 3F, BorderMode.IN).getMode());
	}

	@Test
	public void passesItsValuesToTheShader() {
		final ShaderPass pass = BorderNodeEffect.create(new Color(0.2F, 0.4F, 0.6F, 1F), 3F, BorderMode.IN).fill(false).toShaderPass(RectNode.create(0D, 0D, 10D, 10D));
		pass.bindForTexture(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertEquals(5F, pass.expansion(), 0F);
		Assert.assertEquals(200, pass.priority());
		Assert.assertEquals(3F, (Float) values.get("u_BorderWidth"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("u_BorderColor"), 0F);
		Assert.assertEquals(0, values.get("u_Fill"));
		Assert.assertEquals(BorderMode.IN.ordinal(), values.get("u_Mode"));
	}

	@Test
	public void replacesEachValue() {
		final Color color = new Color(0.6F, 0.4F, 0.2F, 1F);
		final BorderNodeEffect effect = BorderNodeEffect.create(new Color(0.2F, 0.4F, 0.6F, 1F), 3F);
		Assert.assertSame(effect, effect.color(color).width(6F).mode(BorderMode.IN).fill(false));
		Assert.assertSame(color, effect.getColorSupplier().get());
		Assert.assertEquals(6F, effect.getWidthSupplier().get(), 0F);
		Assert.assertSame(BorderMode.IN, effect.getMode());
		Assert.assertFalse(effect.isFill());
	}

	@Test
	public void readsItsSuppliedValuesForEachPass() {
		final Color[] color = {new Color(0.2F, 0.4F, 0.6F, 1F)};
		final float[] width = {3F};
		final BorderNodeEffect effect = BorderNodeEffect.create(new Color(0.2F, 0.4F, 0.6F, 1F), 1F);
		Assert.assertSame(effect, effect.color(() -> color[0]).width(() -> width[0]));
		color[0] = new Color(0.6F, 0.4F, 0.2F, 0.5F);
		width[0] = 8F;
		final ShaderPass pass = effect.toShaderPass(RectNode.create(0D, 0D, 10D, 10D));
		pass.bindForTexture(this.context());
		Assert.assertEquals(10F, pass.expansion(), 0F);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 0.5F}, (float[]) this.shader.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void spreadsTheBorderAroundTheNode() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(BorderNodeEffect.create(new Color(0.6F, 0.4F, 0.2F, 1F), 3F)))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertEquals(95D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(95D, quad.getTop(), 1E-3D);
		Assert.assertEquals(305D, quad.getRight(), 1E-3D);
		Assert.assertEquals(155D, quad.getBottom(), 1E-3D);
		Assert.assertArrayEquals(new float[] {100F, 100F, 300F, 150F}, (float[]) this.shader.getValues().get("u_Rect"), 0F);
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 50D, 5D, this.bridges.getRender().getPixelGrid());
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