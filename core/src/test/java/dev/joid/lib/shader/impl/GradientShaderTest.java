package dev.joid.lib.shader.impl;

import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.Color;

public class GradientShaderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) GradientShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void sharesOneAvailableInstance() {
		Assert.assertSame(GradientShader.inst(), GradientShader.inst());
		Assert.assertTrue(GradientShader.inst().isAvailable());
	}

	@Test
	public void paintsADrawWithTheGradient() {
		final IShader[] bound = new IShader[1];
		GradientShader.use(new Vector2f(0F, 0.25F), new Vector2f(1F, 0.75F), new Color(0.2F, 0.4F, 0.6F, 1F), new Color(0.8F, 0.6F, 0.4F, 0.5F), () -> bound[0] = this.bridges.getRender().getShader(), new Vector4f(10F, 20F, 110F, 70F));
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {0F, 0.25F}, (float[]) values.get("startPos"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0.75F}, (float[]) values.get("endPos"), 0F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("startColor"), 0F);
		Assert.assertArrayEquals(new float[] {0.8F, 0.6F, 0.4F, 0.5F}, (float[]) values.get("endColor"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) values.get("canvas"), 0F);
		Assert.assertEquals(0, values.get("hasTexture"));
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void tintsATexture() {
		GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.BLUE, true, null, new Vector4f(0F, 0F, 1F, 1F));
		Assert.assertEquals(1, this.shader.getValues().get("hasTexture"));
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void releasesTheShaderWhenTheDrawFails() {
		try {
			GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.BLUE, () -> {
				throw new IllegalStateException("draw");
			}, new Vector4f(0F, 0F, 1F, 1F));
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("draw", exception.getMessage());
		}
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void skipsTheDrawWithoutShader() {
		final boolean[] ran = {false};
		GradientShader.inst().shader = null;
		try {
			GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.BLUE, () -> ran[0] = true, new Vector4f(0F, 0F, 1F, 1F));
		} finally {
			GradientShader.inst().shader = this.shader;
		}
		Assert.assertFalse(ran[0]);
		Assert.assertTrue(this.shader.getValues().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCanvas() {
		GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.BLUE, null, null);
	}

}