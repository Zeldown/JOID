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
	public void sharesOneAvailableInstance() {
		Assert.assertSame(ShadowShader.inst(), ShadowShader.inst());
		Assert.assertTrue(ShadowShader.inst().isAvailable());
	}

	@Test
	public void bindsTheRadiusTheBlurAndTheBox() {
		ShadowShader.inst().bind(6F, 12F, 1F, 2F, 3F, 4F);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, this.bridges.getRender().getShader());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertEquals(12F, (Float) values.get("u_Blur"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_Box"), 0F);
	}

	@Test
	public void wrapsADrawInTheShader() {
		final IShader[] bound = new IShader[1];
		ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, () -> bound[0] = this.bridges.getRender().getShader());
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {10F, 20F, 30F, 40F}, (float[]) this.shader.getValues().get("u_Box"), 0F);
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void releasesTheShaderWhenTheDrawFails() {
		try {
			ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, () -> {
				throw new IllegalStateException("draw");
			});
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("draw", exception.getMessage());
		}
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void skipsTheDrawWithoutShader() {
		final boolean[] ran = {false};
		ShadowShader.inst().shader = null;
		try {
			Assert.assertFalse(ShadowShader.inst().isAvailable());
			ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, () -> ran[0] = true);
		} finally {
			ShadowShader.inst().shader = this.shader;
		}
		Assert.assertFalse(ran[0]);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingDraw() {
		ShadowShader.use(5F, 8F, 10F, 20F, 30F, 40F, null);
	}

}