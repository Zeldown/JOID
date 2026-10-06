package dev.joid.lib.shader.pipeline.pass;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.RoundedShader.RoundedShaderType;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.Node;
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
	public void roundsAFixedBox() {
		new RoundedShaderPass(6F, 1F, 2F, 3F, 4F).bindDirect(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(6F, (Float) values.get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 2F, 3F, 4F}, (float[]) values.get("u_InnerRect"), 0F);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), values.get("u_Type"));
	}

	@Test
	public void roundsTheRenderedTexture() {
		new RoundedShaderPass(6F, 1F, 2F, 3F, 4F).bindForTexture(this.context());
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("u_Type"));
	}

	@Test
	public void insetsTheBoxOfItsNodeByTheRadius() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F), RectNode.create(10D, 20D, 100D, 60D)).bindDirect(this.context());
		Assert.assertEquals(5F, (Float) this.shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {15F, 25F, 105F, 75F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void keepsTheSquareCornersOnTheEdges() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F, false, true, false, true), RectNode.create(10D, 20D, 100D, 60D)).bindForTexture(this.context());
		Assert.assertArrayEquals(new float[] {10F, 20F, 105F, 75F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void keepsTheSquareCornersOnTheOtherEdges() {
		new RoundedShaderPass(RoundedNodeEffect.create(5F, true, false, true, false), RectNode.create(10D, 20D, 100D, 60D)).bindForTexture(this.context());
		Assert.assertArrayEquals(new float[] {15F, 25F, 110F, 80F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void readsTheEffectWhenItBinds() {
		final float[] radius = {5F};
		final RoundedShaderPass pass = new RoundedShaderPass(RoundedNodeEffect.<Node>create(() -> radius[0]), RectNode.create(10D, 20D, 100D, 60D));
		radius[0] = 8F;
		pass.bindDirect(this.context());
		Assert.assertArrayEquals(new float[] {18F, 28F, 102F, 72F}, (float[]) this.shader.getValues().get("u_InnerRect"), 1E-3F);
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
		pass.bindDirect(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 60D, 0D, this.bridges.getRender().getPixelGrid());
	}

}