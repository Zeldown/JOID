package dev.joid.lib.shader.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.shader.impl.RoundedShader.RoundedShaderType;

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
	public void sharesOneAvailableInstance() {
		Assert.assertSame(RoundedShader.inst(), RoundedShader.inst());
		Assert.assertTrue(RoundedShader.inst().isAvailable());
	}

	@Test
	public void bindsTheRadiusAndTheInnerBox() {
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F);
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertSame(this.shader, this.bridges.getRender().getShader());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_InnerRect"), 0F);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), values.get("u_Type"));
	}

	@Test
	public void bindsTheRequestedType() {
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F, RoundedShaderType.TEXTURE);
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("u_Type"));
		RoundedShader.inst().bind(6F, 1F, 2F, 3F, 4F, RoundedShaderType.COLOR);
		Assert.assertEquals(RoundedShaderType.COLOR.ordinal(), this.shader.getValues().get("u_Type"));
	}

	@Test
	public void wrapsADrawInTheShader() {
		final IShader[] bound = new IShader[1];
		RoundedShader.use(5F, 10F, 20F, 30F, 40F, () -> bound[0] = this.bridges.getRender().getShader());
		Assert.assertSame(this.shader, bound[0]);
		Assert.assertArrayEquals(new float[] {10F, 20F, 30F, 40F}, (float[]) this.shader.getValues().get("u_InnerRect"), 0F);
		Assert.assertFalse(this.shader.isBound());
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void releasesTheShaderWhenTheDrawFails() {
		try {
			RoundedShader.use(5F, 10F, 20F, 30F, 40F, () -> {
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
		RoundedShader.inst().shader = null;
		try {
			Assert.assertFalse(RoundedShader.inst().isAvailable());
			RoundedShader.use(5F, 10F, 20F, 30F, 40F, () -> ran[0] = true);
		} finally {
			RoundedShader.inst().shader = this.shader;
		}
		Assert.assertFalse(ran[0]);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingDraw() {
		RoundedShader.use(5F, 10F, 20F, 30F, 40F, null);
	}

}