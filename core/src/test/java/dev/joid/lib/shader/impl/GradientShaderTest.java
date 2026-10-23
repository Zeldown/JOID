package dev.joid.lib.shader.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;

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
	public void paintsADrawWithTheGradient() {
		final IShader[] bound = new IShader[1];
		GradientShader.use(Point.of(0D, 0.25D), Point.of(1D, 0.75D), new Color(0.2F, 0.4F, 0.6F, 1F), new Color(0.8F, 0.6F, 0.4F, 0.5F), () -> bound[0] = this.bridges.getRender().getState().getShader(), BoundingBox.create(10D, 20D, 100D, 50D));
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {0F, 0.25F}, (float[]) values.get("u_GradientStartPos"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0.75F}, (float[]) values.get("u_GradientEndPos"), 0F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("u_GradientStart"), 0F);
		Assert.assertArrayEquals(new float[] {0.8F, 0.6F, 0.4F, 0.5F}, (float[]) values.get("u_GradientEnd"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) values.get("u_GradientCanvas"), 0F);
		Assert.assertEquals(0, values.get("hasTexture"));
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void paintsTheCurrentFrameOfAnAnimatedStop() {
		final Color animated = new Color(1F, 1F, 1F, 1F, color -> new Color(0.2F, 0.4F, 0.6F, 1F));
		GradientShader.use(Point.of(0D, 0D), Point.of(1D, 0D), animated, Color.BLUE, () -> {}, BoundingBox.create(0D, 0D, 1D, 1D));
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) this.shader.getValues().get("u_GradientStart"), 0F);
	}

	@Test
	public void tintsATexture() {
		GradientShader.use(Point.of(0D, 0D), Point.of(1D, 0D), Color.RED, Color.BLUE, true, () -> {}, BoundingBox.create(0D, 0D, 1D, 1D));
		Assert.assertEquals(1, this.shader.getValues().get("hasTexture"));
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void restoresThePreviousShader() {
		final RecordingShader previous = new RecordingShader();
		previous.bind();
		GradientShader.use(Point.of(0D, 0D), Point.of(1D, 0D), Color.RED, Color.BLUE, () -> {}, BoundingBox.create(0D, 0D, 1D, 1D));
		Assert.assertSame(previous, this.bridges.getRender().getState().getShader());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCanvas() {
		GradientShader.use(Point.of(0D, 0D), Point.of(1D, 0D), Color.RED, Color.BLUE, null, null);
	}

}