package dev.joid.lib.color;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;

public class ColorGradientTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsItsEndsAndItsDirection() {
		final Point startPoint = Point.create(1D, 0D);
		final Point endPoint = Point.create(0D, 1D);
		final ColorGradient gradient = new ColorGradient(Color.RED, Color.BLUE, startPoint, endPoint);
		Assert.assertSame(Color.RED, gradient.getStartColor());
		Assert.assertSame(Color.BLUE, gradient.getEndColor());
		Assert.assertSame(startPoint, gradient.getStartPoint());
		Assert.assertSame(endPoint, gradient.getEndPoint());
	}

	@Test
	public void drawsThroughTheGradientShader() {
		final ColorGradient gradient = new ColorGradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), Point.create(0D, 0D), Point.create(1D, 0D));
		final AtomicReference<IShader> used = new AtomicReference<>();
		gradient.use(() -> used.set(this.bridges.getRender().getState().getShader()), BoundingBox.create(10D, 20D, 100D, 50D));
		final RecordingShader shader = (RecordingShader) used.get();
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 1F}, (float[]) shader.getValues().get("u_GradientStart"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F, 1F, 0.5F}, (float[]) shader.getValues().get("u_GradientEnd"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) shader.getValues().get("u_GradientCanvas"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) shader.getValues().get("u_GradientEndPos"), 0F);
		Assert.assertEquals(0, shader.getValues().get("hasTexture"));
		Assert.assertNotSame(shader, this.bridges.getRender().getState().getShader());
	}

}