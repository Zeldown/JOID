package dev.joid.lib.shader.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;

public class CircleShaderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) CircleShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void sharesOneAvailableInstance() {
		Assert.assertSame(CircleShader.inst(), CircleShader.inst());
		Assert.assertTrue(CircleShader.inst().isAvailable());
	}

	@Test
	public void bindsTheRadiusAndTheCenter() {
		CircleShader.inst().bind(12F, 30F, 40F);
		Assert.assertSame(this.shader, this.bridges.getRender().getState().getShader());
		Assert.assertEquals(12F, (Float) this.shader.getValues().get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {30F, 40F}, (float[]) this.shader.getValues().get("center"), 0F);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), this.shader.getValues().get("type"));
	}

	@Test
	public void bindsTheRequestedType() {
		CircleShader.inst().bind(12F, 30F, 40F, RoundedShaderType.TEXTURE);
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("type"));
		CircleShader.inst().bind(12F, 30F, 40F, RoundedShaderType.COLOR);
		Assert.assertEquals(RoundedShaderType.COLOR.ordinal(), this.shader.getValues().get("type"));
	}

	@Test
	public void wrapsADrawInTheShader() {
		final IShader[] bound = new IShader[1];
		CircleShader.use(8F, 1F, 2F, () -> bound[0] = this.bridges.getRender().getState().getShader());
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertEquals(8F, (Float) this.shader.getValues().get("radius"), 0F);
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void bindsAndReleasesWithoutDraw() {
		CircleShader.use(8F, 1F, 2F, null);
		Assert.assertArrayEquals(new float[] {1F, 2F}, (float[]) this.shader.getValues().get("center"), 0F);
		Assert.assertFalse(this.shader.isBound());
	}

	@Test
	public void releasesTheShaderWhenTheDrawFails() {
		try {
			CircleShader.use(8F, 1F, 2F, () -> {
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
		this.shader.setActive(false);
		try {
			Assert.assertFalse(CircleShader.inst().isAvailable());
			CircleShader.use(8F, 1F, 2F, () -> ran[0] = true);
		} finally {
			this.shader.setActive(true);
		}
		Assert.assertFalse(ran[0]);
	}

}