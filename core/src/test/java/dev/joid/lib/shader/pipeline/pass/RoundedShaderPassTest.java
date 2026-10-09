package dev.joid.lib.shader.pipeline.pass;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.RoundedShaderType;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class RoundedShaderPassTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) RoundedShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void roundsTheRenderedTextureOfAFixedBox() {
		new RoundedShaderPass(6F, 1F, 2F, 3F, 4F).bind(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_InnerRect"), 0F);
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), values.get("u_Type"));
	}

	@Test
	public void insetsTheBoxOfItsNodeByTheRadius() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F), RectNode.create(10D, 20D, 100D, 60D)).bind(this.context());
		Assert.assertEquals(5F, (Float) this.shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {15F, 25F, 105F, 75F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void keepsTheSquareCornersOnTheEdges() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F, false, false, true, true), RectNode.create(10D, 20D, 100D, 60D)).bind(this.context());
		Assert.assertArrayEquals(new float[] {10F, 20F, 105F, 75F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void keepsTheSquareCornersOnTheOtherEdges() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F, true, true, false, false), RectNode.create(10D, 20D, 100D, 60D)).bind(this.context());
		Assert.assertArrayEquals(new float[] {15F, 25F, 110F, 80F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void readsTheEffectWhenItBinds() {
		final float[] radius = {5F};
		final RoundedShaderPass pass = new RoundedShaderPass(RoundedNodeEffect.create(() -> radius[0]), RectNode.create(10D, 20D, 100D, 60D));
		radius[0] = 8F;
		pass.bind(this.context());
		Assert.assertArrayEquals(new float[] {18F, 28F, 102F, 72F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void rampsItsEdgesWithoutSnappingWhenRotated() {
		new RoundedShaderPass(6F, 1F, 2F, 3F, 4F).bind(this.context());
		Assert.assertEquals(1, this.shader.getValues().get("u_Aligned"));
		this.bridges.getRender().pushMatrix();
		try {
			this.bridges.getRender().rotate(10D, 0D, 0D, 1D);
			new RoundedShaderPass(RoundedNodeEffect.create(5F), RectNode.create(10D, 20D, 100D, 60D)).bind(this.context());
		} finally {
			this.bridges.getRender().popMatrix();
		}
		Assert.assertEquals(0, this.shader.getValues().get("u_Aligned"));
	}

	@Test
	public void runsBeforeTheOtherPassesWithoutExpanding() {
		final RoundedShaderPass pass = new RoundedShaderPass(6F, 1F, 2F, 3F, 4F);
		Assert.assertEquals(100, pass.priority());
		Assert.assertEquals(0F, pass.expansion(), 0F);
	}

	@Test
	public void releasesTheShader() {
		final RoundedShaderPass pass = new RoundedShaderPass(6F, 1F, 2F, 3F, 4F);
		pass.bind(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 60D, 0D, this.bridges.getRender().getPixelGrid());
	}

}