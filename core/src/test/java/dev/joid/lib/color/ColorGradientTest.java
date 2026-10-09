package dev.joid.lib.color;

import java.util.concurrent.atomic.AtomicReference;

import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;

public class ColorGradientTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsItsEndsAndItsDirection() {
		final Vector4f direction = new Vector4f(1F, 0F, 0F, 1F);
		final ColorGradient gradient = new ColorGradient(Color.RED, Color.BLUE, direction);
		Assert.assertSame(Color.RED, gradient.getStartColor());
		Assert.assertSame(Color.BLUE, gradient.getEndColor());
		Assert.assertSame(direction, gradient.getDirection());
	}

	@Test
	public void drawsThroughTheGradientShader() {
		final ColorGradient gradient = new ColorGradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), new Vector4f(0F, 0F, 1F, 0F));
		final AtomicReference<IShader> used = new AtomicReference<>();
		gradient.use(() -> used.set(this.bridges.getRender().getState().getShader()), new Vector4f(10F, 20F, 110F, 70F));
		final RecordingShader shader = (RecordingShader) used.get();
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 1F}, (float[]) shader.getValues().get("startColor"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F, 1F, 0.5F}, (float[]) shader.getValues().get("endColor"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) shader.getValues().get("canvas"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) shader.getValues().get("endPos"), 0F);
		Assert.assertEquals(0, shader.getValues().get("hasTexture"));
		Assert.assertNotSame(shader, this.bridges.getRender().getState().getShader());
	}

}