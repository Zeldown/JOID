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
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;

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
		GradientShader.use(Point.create(0D, 0.25D), Point.create(1D, 0.75D), new Color(0.2F, 0.4F, 0.6F, 1F), new Color(0.8F, 0.6F, 0.4F, 0.5F), () -> bound[0] = this.bridges.getRender().getState().getShader(), BoundingBox.create(10D, 20D, 100D, 50D));
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
	public void tintsATexture() {
		GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.BLUE, true, () -> {}, BoundingBox.create(0D, 0D, 1D, 1D));
		Assert.assertEquals(1, this.shader.getValues().get("hasTexture"));
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void releasesTheShaderWhenTheDrawFails() {
		try {
			GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.BLUE, () -> {
				throw new IllegalStateException("draw");
			}, BoundingBox.create(0D, 0D, 1D, 1D));
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("draw", exception.getMessage());
		}
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void restoresThePreviousShader() {
		final RecordingShader previous = new RecordingShader();
		previous.bind();
		GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.BLUE, () -> {}, BoundingBox.create(0D, 0D, 1D, 1D));
		Assert.assertSame(previous, this.bridges.getRender().getState().getShader());
	}

	@Test
	public void skipsTheDrawWithoutShader() {
		final boolean[] ran = {false};
		this.shader.setActive(false);
		try {
			GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.BLUE, () -> ran[0] = true, BoundingBox.create(0D, 0D, 1D, 1D));
		} finally {
			this.shader.setActive(true);
		}
		Assert.assertFalse(ran[0]);
		Assert.assertTrue(this.shader.getValues().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCanvas() {
		GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.BLUE, null, null);
	}

}