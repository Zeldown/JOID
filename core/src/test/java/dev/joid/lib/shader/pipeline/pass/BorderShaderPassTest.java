package dev.joid.lib.shader.pipeline.pass;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;

public class BorderShaderPassTest {

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
		final BorderShaderPass pass = new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 1F));
		pass.bindDirect(this.context());
		Assert.assertEquals(1, this.shader.getValues().get("u_Fill"));
		Assert.assertEquals(BorderMode.OUT.ordinal(), this.shader.getValues().get("u_Mode"));
		Assert.assertEquals(200, pass.priority());
		Assert.assertEquals(5F, pass.expansion(), 0F);
	}

	@Test
	public void leavesTheInsideEmpty() {
		new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 1F), false).bindForTexture(this.context());
		Assert.assertEquals(0, this.shader.getValues().get("u_Fill"));
		Assert.assertEquals(BorderMode.OUT.ordinal(), this.shader.getValues().get("u_Mode"));
	}

	@Test
	public void drawsAnInnerBorder() {
		new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 1F), true, BorderMode.IN).bindForTexture(this.context());
		Assert.assertEquals(BorderMode.IN.ordinal(), this.shader.getValues().get("u_Mode"));
	}

	@Test
	public void outlinesTheBoxOfItsContext() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final ShaderPassContext context = this.context();
		new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 0.5F)).bindDirect(context);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(3F * 1366F / 1920F, (Float) values.get("u_BorderWidth"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 0.5F}, (float[]) values.get("u_BorderColor"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) values.get("u_Rect"), 0F);
		Assert.assertArrayEquals(new float[] {context.getTexelWidth(), context.getTexelHeight()}, (float[]) values.get("u_TexelSize"), 0F);
		Assert.assertEquals(0, values.get("u_HasGradient"));
	}

	@Test
	public void spreadsAGradientOverTheBox() {
		new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 1F).toGradient(new Color(0.6F, 0.4F, 0.2F, 1F))).bindDirect(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertEquals(1, values.get("u_HasGradient"));
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("u_GradientStart"), 0F);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) values.get("u_GradientEnd"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F}, (float[]) values.get("u_GradientStartPos"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) values.get("u_GradientEndPos"), 0F);
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) values.get("u_GradientCanvas"), 0F);
	}

	@Test
	public void releasesTheShader() {
		final BorderShaderPass pass = new BorderShaderPass(3F, new Color(0.2F, 0.4F, 0.6F, 1F));
		pass.bindDirect(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 50D, 5D, this.bridges.getRender().getPixelGrid());
	}

}