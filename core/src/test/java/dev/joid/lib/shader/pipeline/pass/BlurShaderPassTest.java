package dev.joid.lib.shader.pipeline.pass;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.ShaderPassContext;

public class BlurShaderPassTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void useAFractionalScale() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().getProjection().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		this.shader = (RecordingShader) BlurShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void expandsByItsRadius() {
		final BlurShaderPass pass = BlurShaderPass.create(6.5F, true);
		Assert.assertEquals(6.5F, pass.expansion(), 0F);
	}

	@Test
	public void blursHorizontallyInWindowPixels() {
		final ShaderPassContext context = this.context();
		BlurShaderPass.create(4F, true).bind(context);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(4F * 1366F / 1920F, (Float) values.get("u_Radius"), 1E-4F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) values.get("u_Direction"), 0F);
		Assert.assertArrayEquals(new float[] {context.getTexelWidth(), context.getTexelHeight()}, (float[]) values.get("u_TexelSize"), 0F);
	}

	@Test
	public void blursVerticallyInWindowPixels() {
		BlurShaderPass.create(4F, false).bind(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertEquals(4F * 768F / 1080F, (Float) values.get("u_Radius"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) values.get("u_Direction"), 0F);
	}

	@Test
	public void releasesTheShader() {
		final BlurShaderPass pass = BlurShaderPass.create(4F, true);
		pass.bind(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		try {
			final Method of = ShaderPassContext.class.getDeclaredMethod("of", double.class, double.class, double.class, double.class, double.class, double.class, PixelGrid.class);
			of.setAccessible(true);
			return (ShaderPassContext) of.invoke(null, 10D, 20D, 100D, 50D, 4D, 0D, this.bridges.getRender().getPixelGrid());
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}