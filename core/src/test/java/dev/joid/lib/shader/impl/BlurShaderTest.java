package dev.joid.lib.shader.impl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;

public class BlurShaderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void sharesOneAvailableInstance() {
		Assert.assertSame(BlurShader.inst(), BlurShader.inst());
		Assert.assertTrue(BlurShader.inst().isAvailable());
	}

	@Test
	public void bindsTheRadiusTheDirectionAndTheTexel() {
		final RecordingShader shader = (RecordingShader) BlurShader.inst().getShader();
		BlurShader.inst().bind(4F, 0F, 1F, 0.01F, 0.02F);
		Assert.assertSame(shader, this.bridges.getRender().getState().getShader());
		Assert.assertEquals(4F, (Float) shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) shader.getValues().get("u_Direction"), 0F);
		Assert.assertArrayEquals(new float[] {0.01F, 0.02F}, (float[]) shader.getValues().get("u_TexelSize"), 0F);
		BlurShader.inst().unbind();
		Assert.assertFalse(shader.isBound());
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

}