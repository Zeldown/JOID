package dev.joid.lib.shader.pipeline.pass;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;

public class BlurShaderPassTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void useAFractionalScale() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		this.shader = (RecordingShader) BlurShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void runsTheHorizontalPassBeforeTheVerticalOne() {
		Assert.assertEquals(150, new BlurShaderPass(4F, true, 0).priority());
		Assert.assertEquals(151, new BlurShaderPass(4F, false, 0).priority());
		Assert.assertEquals(152, new BlurShaderPass(4F, true, 1).priority());
		Assert.assertEquals(153, new BlurShaderPass(4F, false, 1).priority());
	}

	@Test
	public void expandsByItsRadius() {
		final BlurShaderPass pass = new BlurShaderPass(6.5F, true, 0);
		Assert.assertEquals(6.5F, pass.expansion(), 0F);
		Assert.assertFalse(pass.supportsDirectBind());
	}

	@Test
	public void blursHorizontallyInWindowPixels() {
		final ShaderPassContext context = this.context();
		new BlurShaderPass(4F, true, 0).bindDirect(context);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(4F * 1366F / 1920F, (Float) values.get("u_Radius"), 1E-4F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) values.get("u_Direction"), 0F);
		Assert.assertArrayEquals(new float[] {context.getTexelWidth(), context.getTexelHeight()}, (float[]) values.get("u_TexelSize"), 0F);
	}

	@Test
	public void blursVerticallyInWindowPixels() {
		new BlurShaderPass(4F, false, 0).bindForTexture(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertEquals(4F * 768F / 1080F, (Float) values.get("u_Radius"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) values.get("u_Direction"), 0F);
	}

	@Test
	public void releasesTheShader() {
		final BlurShaderPass pass = new BlurShaderPass(4F, true, 0);
		pass.bindForTexture(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 50D, 4D, this.bridges.getRender().getPixelGrid());
	}

}