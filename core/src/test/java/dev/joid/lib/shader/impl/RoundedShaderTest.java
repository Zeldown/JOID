package dev.joid.lib.shader.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;

public class RoundedShaderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) RoundedShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void bindsTheRadiusAndTheInnerBox() {
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, this.bridges.getRender().getState().getShader());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_InnerRect"), 0F);
		Assert.assertEquals(ShaderFillType.AUTO.ordinal(), values.get("u_Type"));
	}

	@Test
	public void fillsWithoutStrokeUntilAsked() {
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F);
		Assert.assertEquals(0F, (Float) this.shader.getValues().get("u_Stroke"), 0F);
		RoundedShader.inst().stroke(2F);
		Assert.assertEquals(2F, (Float) this.shader.getValues().get("u_Stroke"), 0F);
	}

	@Test
	public void bindsTheRequestedType() {
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F, ShaderFillType.TEXTURE);
		Assert.assertEquals(ShaderFillType.TEXTURE.ordinal(), this.shader.getValues().get("u_Type"));
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F, ShaderFillType.COLOR);
		Assert.assertEquals(ShaderFillType.COLOR.ordinal(), this.shader.getValues().get("u_Type"));
	}

	@Test
	public void wrapsADrawInTheShader() {
		final IShader[] bound = new IShader[1];
		RoundedShader.use(5F, 10F, 20F, 30F, 40F, () -> bound[0] = this.bridges.getRender().getState().getShader());
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {10F, 20F, 30F, 40F}, (float[]) this.shader.getValues().get("u_InnerRect"), 0F);
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingDraw() {
		RoundedShader.use(5F, 10F, 20F, 30F, 40F, null);
	}

}