package dev.joid.lib.shader.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;

public class ShadowShaderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) ShadowShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void bindsTheRadiusTheBlurAndTheBox() {
		ShadowShader.inst().bind(6F, 12F, 1F, 2F, 3F, 4F);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, this.bridges.getRender().getState().getShader());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertEquals(12F, (Float) values.get("u_Blur"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_Box"), 0F);
	}

	@Test
	public void wrapsADrawInTheShader() {
		final IShader[] bound = new IShader[1];
		ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, () -> bound[0] = this.bridges.getRender().getState().getShader());
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {10F, 20F, 30F, 40F}, (float[]) this.shader.getValues().get("u_Box"), 0F);
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingDraw() {
		ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, null);
	}

}